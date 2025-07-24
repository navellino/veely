package com.veely.repository;


import com.veely.entity.FuelCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelCardRepository extends JpaRepository<FuelCard, Long> {
}
