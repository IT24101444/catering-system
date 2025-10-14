package com.example.catering_system.service;

import com.example.catering_system.model.Employee;
import com.example.catering_system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee saveEmployee(Employee employee) {
        if (employee.getEmployeeCode() == null || employee.getEmployeeCode().isEmpty()) {
            employee.setEmployeeCode(generateEmployeeCode(employee));
        }
        if (employee.getInSalarySystem() == null) {
            employee.setInSalarySystem(false);
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updated) {
        return employeeRepository.findById(id)
                .map(existing -> {
                    existing.setFirstName(updated.getFirstName());
                    existing.setLastName(updated.getLastName());
                    existing.setEmail(updated.getEmail());
                    existing.setPhone(updated.getPhone());
                    existing.setAddress(updated.getAddress());
                    existing.setDepartment(updated.getDepartment());
                    existing.setPosition(updated.getPosition());
                    existing.setBaseSalary(updated.getBaseSalary());
                    return employeeRepository.save(existing);
                }).orElse(null);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    private String generateEmployeeCode(Employee employee) {
        String prefix = "EMP";
        String initials = "";
        if (employee.getFirstName() != null && !employee.getFirstName().isEmpty()) {
            initials += Character.toUpperCase(employee.getFirstName().charAt(0));
        }
        if (employee.getLastName() != null && !employee.getLastName().isEmpty()) {
            initials += Character.toUpperCase(employee.getLastName().charAt(0));
        }
        long count = employeeRepository.count();
        return prefix + initials + String.format("%05d", count + 1);
    }
}
