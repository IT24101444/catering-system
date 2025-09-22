package com.cateringsystem.catering_system.repository;

import com.cateringsystem.catering_system.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
}
