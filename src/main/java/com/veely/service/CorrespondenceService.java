package com.veely.service;

import com.veely.entity.Correspondence;
import com.veely.model.CorrespondenceType;
import com.veely.repository.CorrespondenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class CorrespondenceService {
    private final CorrespondenceRepository repo;

    public Correspondence register(CorrespondenceType tipo,
            String descrizione,
            LocalDate data,
            String sender,
            String recipient,
            String notes) {
        int anno = LocalDate.now().getYear();
        Integer max = repo.findMaxProgressivo(anno, tipo);
        int progressivo = (max == null) ? 1 : max + 1;

        Correspondence c = Correspondence.builder()
                .anno(anno)
                .progressivo(progressivo)
                .tipo(tipo)
                .descrizione(descrizione)
                .data(data)
                .sender(sender)
                .recipient(recipient)
                .notes(notes)
                .build();
        return repo.save(c);
    }

    public String formatProtocol(Correspondence c) {
        return String.format("%03d/%d", c.getProgressivo(), c.getAnno());
    }

    @Transactional(readOnly = true)
    public java.util.List<Correspondence> getAll() {
        return repo.findAll();
    }
}