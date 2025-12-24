package com.tathang.example304.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tathang.example304.model.CoffeeTable;

import java.util.List;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Long> {
    List<CoffeeTable> findByStatus(CoffeeTable.TableStatus status);
    CoffeeTable findByNumber(Integer number);
    List<CoffeeTable> findByCapacityGreaterThanEqual(Integer capacity);
}