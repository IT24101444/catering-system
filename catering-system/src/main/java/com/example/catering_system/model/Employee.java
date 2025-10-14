package com.example.catering_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "employee_code", unique = true)
    private String employeeCode;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
    private String phone;
    @Column(name = "address")
    private String address;
    @Column(name = "position")
    @NotBlank(message = "Position is required")
    private String position;
    @Column(name = "department")
    @NotBlank(message = "Department name is required")
    private String department;
    @Column(name = "base_salary")
    private Double baseSalary;
    @Column(name = "join_date")
    private java.util.Date joinDate;
    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "employee", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<StaffPayment> payments;
    @Column(name = "in_salary_system")
    private Boolean inSalarySystem;
    @Column(name = "monthly_salary")
    private Double monthlySalary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmployeeCode() {
        return employeeCode;
    }
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }
    public java.util.Date getJoinDate() {
        return joinDate;
    }
    public void setJoinDate(java.util.Date joinDate) {
        this.joinDate = joinDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.List<StaffPayment> getPayments() {
        return payments;
    }

    public void setPayments(java.util.List<StaffPayment> payments) {
        this.payments = payments;
    }

    public Boolean getInSalarySystem() {
        return inSalarySystem;
    }

    public void setInSalarySystem(Boolean inSalarySystem) {
        this.inSalarySystem = inSalarySystem;
    }

    public Double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(Double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }
}
