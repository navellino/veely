package com.veely.service;


import com.veely.entity.Document;
import com.veely.entity.Vehicle;
import com.veely.exception.ResourceNotFoundException;
import com.veely.model.DocumentType;
import com.veely.model.VehicleStatus;
import com.veely.repository.DocumentRepository;
import com.veely.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servizio per la gestione completa dei veicoli,
 * inclusi calcolo totale e upload/download di foto e documenti.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepo;
    private final DocumentRepository documentRepo;
    private final FileSystemStorageService fileStorage;

    // ---------------------- CRUD VEICOLO ----------------------

    public Vehicle create(Vehicle payload) {
        payload.setStatus(VehicleStatus.IN_SERVICE);
        BigDecimal financial = safe(payload.getFinancialFee());
        BigDecimal assistance = safe(payload.getAssistanceFee());
        payload.setTotalFee(financial.add(assistance));
        return vehicleRepo.save(payload);
    }

    public Vehicle update(Long id, Vehicle payload) {
        Vehicle existing = findByIdOrThrow(id);
        existing.setPlate(payload.getPlate());
        existing.setChassisNumber(payload.getChassisNumber());
        existing.setBrand(payload.getBrand());
        existing.setModel(payload.getModel());
        existing.setSeries(payload.getSeries());
        existing.setYear(payload.getYear());
        existing.setType(payload.getType());
        existing.setFuelType(payload.getFuelType());
        existing.setOwnership(payload.getOwnership());
        existing.setSupplier(payload.getSupplier());
        existing.setRegistrationDate(payload.getRegistrationDate());
        existing.setContractStartDate(payload.getContractStartDate());
        existing.setContractEndDate(payload.getContractEndDate());
        existing.setContractDuration(payload.getContractDuration());
        existing.setContractualKm(payload.getContractualKm());
        existing.setFinancialFee(safe(payload.getFinancialFee()));
        existing.setAssistanceFee(safe(payload.getAssistanceFee()));
        existing.setTotalFee(existing.getFinancialFee().add(existing.getAssistanceFee()));
        existing.setAnnualFringeBenefit(payload.getAnnualFringeBenefit());
        existing.setMonthlyFringeBenefit(payload.getMonthlyFringeBenefit());
        existing.setStatus(payload.getStatus());
        existing.setCurrentMileage(payload.getCurrentMileage());
        existing.setFuelCard(payload.getFuelCard());
        existing.setFuelCardExpiryDate(payload.getFuelCardExpiryDate());
        existing.setTelepass(payload.getTelepass());
        existing.setInsuranceExpiryDate(payload.getInsuranceExpiryDate());
        existing.setCarTaxExpiryDate(payload.getCarTaxExpiryDate());
        existing.setImagePath(payload.getImagePath());
        return existing;
    }

    @Transactional(readOnly = true)
    public Vehicle findByIdOrThrow(Long id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veicolo non trovato: " + id));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return vehicleRepo.findAll();
    }

    public void delete(Long id) {
        Vehicle v = findByIdOrThrow(id);
        fileStorage.deleteDirectory("vehicles/" + id);
        vehicleRepo.delete(v);
    }

    // ---------------------- FOTO VEICOLO ----------------------

    /**
     * Carica una fotografia del veicolo in uploads/vehicles/{id}/photos.
     * Registra un Document con type = VEHICLE_IMAGE.
     */
    public Document uploadPhoto(Long vehicleId, MultipartFile file) {
        Vehicle v = findByIdOrThrow(vehicleId);
        String subDir = "vehicles/" + vehicleId + "/photos";
        String filename = fileStorage.store(file, subDir);

        Document doc = Document.builder()
                .vehicle(v)
                .type(DocumentType.VEHICLE_IMAGE)
                .path(subDir + "/" + filename)
                .issueDate(LocalDate.now())
                .expiryDate(null)
                .build();
        return documentRepo.save(doc);
    }

    @Transactional(readOnly = true)
    public Resource loadPhoto(Long vehicleId, String filename) {
        String subDir = "vehicles/" + vehicleId + "/photos";
        return fileStorage.loadAsResource(filename, subDir);
    }

    // ---------------------- DOCUMENTI VEICOLO ----------------------

    /**
     * Carica un documento generico (leasing, assicurazione, manutenzione…)
     * in uploads/vehicles/{id}/docs.
     */
    public Document uploadDocument(Long vehicleId,
                                   MultipartFile file,
                                   DocumentType type,
                                   LocalDate issueDate,
                                   LocalDate expiryDate) {
        Vehicle v = findByIdOrThrow(vehicleId);
        String subDir = "vehicles/" + vehicleId + "/docs";
        String filename = fileStorage.store(file, subDir);

        Document doc = Document.builder()
                .vehicle(v)
                .type(type)
                .path(subDir + "/" + filename)
                .issueDate(issueDate)
                .expiryDate(expiryDate)
                .build();
        return documentRepo.save(doc);
    }

    @Transactional(readOnly = true)
    public Resource loadDocument(Long vehicleId, String filename) {
        String subDir = "vehicles/" + vehicleId + "/docs";
        return fileStorage.loadAsResource(filename, subDir);
    }

    /**
     * Elimina un documento veicolo dal DB e dal filesystem.
     */
    public void deleteDocument(Long docId) {
        Document doc = documentRepo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento non trovato: " + docId));

        String fullPath = doc.getPath(); // es. "vehicles/12/docs/123_filename.pdf"
        int lastSlash = fullPath.lastIndexOf('/');
        String subDir = fullPath.substring(0, lastSlash);
        String filename = fullPath.substring(lastSlash + 1);

        fileStorage.delete(filename, subDir);
        documentRepo.delete(doc);
    }
    
    /**
     * Aggiorna il chilometraggio corrente del veicolo.
     *
     * @param vehicleId  id del veicolo da aggiornare
     * @param mileage    nuovo valore dei km percorsi
     * @return entità Vehicle aggiornata
     */
    public Vehicle updateMileage(Long vehicleId, int mileage) {
        Vehicle vehicle = findByIdOrThrow(vehicleId);
        vehicle.setCurrentMileage(mileage);
        return vehicle;
    }

    // ---------------------- UTILI INTERNI ----------------------

    private BigDecimal safe(BigDecimal x) {
        return (x != null) ? x : BigDecimal.ZERO;
    }
    
    /** Aggiorna la data di scadenza assicurazione. */
    public void updateInsuranceExpiry(Long vehicleId, LocalDate newDate) {
        Vehicle v = findByIdOrThrow(vehicleId);
        v.setInsuranceExpiryDate(newDate);
    }

    /** Aggiorna la data di scadenza bollo. */
    public void updateCarTaxExpiry(Long vehicleId, LocalDate newDate) {
        Vehicle v = findByIdOrThrow(vehicleId);
        v.setCarTaxExpiryDate(newDate);
    }

    /** Aggiorna la data di scadenza della fuel card. */
    public void updateFuelCardExpiry(Long vehicleId, LocalDate newDate) {
        Vehicle v = findByIdOrThrow(vehicleId);
        v.setFuelCardExpiryDate(newDate);
    }
}