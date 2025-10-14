package com.example.catering_system.controller;

import com.example.catering_system.model.Employee;
import com.example.catering_system.model.StaffPayment;
import com.example.catering_system.service.EmployeeService;
import com.example.catering_system.service.StaffPaymentService;
import com.example.catering_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.catering_system.service.InvoiceService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private StaffPaymentService staffPaymentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    @Qualifier("mainOrderService")
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        long inSalaryCount = employees.stream().filter(e -> Boolean.TRUE.equals(e.getInSalarySystem())).count();
        java.time.LocalDate now = java.time.LocalDate.now();
        int y = now.getYear();
        int m = now.getMonthValue();
        long totalEmployees = employees.size();
        long newCandidates = employees.stream().filter(e -> !Boolean.TRUE.equals(e.getInSalarySystem())).count();
        // this month payments
        List<StaffPayment> thisMonthPayments = staffPaymentService.getPaymentsByYearMonth(y, m);
        double totalPaid = thisMonthPayments.stream().filter(p -> p.getPaymentYear() != null && p.getPaymentMonth() != null && p.getPaymentYear() == y && p.getPaymentMonth() == m)
                .mapToDouble(StaffPayment::getAmountPaid).sum();
        long paidEmployeeIds = thisMonthPayments.stream()
                .filter(p -> p.getPaymentYear() != null && p.getPaymentMonth() != null && p.getPaymentYear() == y && p.getPaymentMonth() == m && "Completed".equalsIgnoreCase(p.getStatus()))
                .map(p -> p.getEmployee() != null ? p.getEmployee().getId() : null)
                .filter(java.util.Objects::nonNull)
                .distinct().count();
        long pendingPayments = inSalaryCount - paidEmployeeIds;

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("newCandidates", newCandidates);
        model.addAttribute("recentPayments", thisMonthPayments.stream().limit(10).collect(Collectors.toList()));
        // Revenue overview (simple example)
        double paid = invoiceService.getAllInvoices().stream().filter(i -> "Paid".equalsIgnoreCase(i.getStatus())).mapToDouble(i -> i.getAmount()).sum();
        double pending = invoiceService.getAllInvoices().stream().filter(i -> "Unpaid".equalsIgnoreCase(i.getStatus()) || "Pending".equalsIgnoreCase(i.getStatus())).mapToDouble(i -> i.getAmount()).sum();
        model.addAttribute("revPaid", paid);
        model.addAttribute("revPending", pending);
        return "finance-dashboard";
    }

    // Finance can only manage salary enrollment/edit, not create employees.
    @GetMapping("/candidates")
    public String candidates(Model model) {
        List<Employee> candidates = employeeService.getAllEmployees().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getInSalarySystem()))
                .collect(Collectors.toList());
        model.addAttribute("candidates", candidates);
        return "finance-candidates";
    }

    @PostMapping("/enroll/{id}")
    public String enroll(@PathVariable Long id, @RequestParam Double monthlySalary) {
        employeeService.getEmployeeById(id).ifPresent(e -> {
            e.setInSalarySystem(true);
            e.setMonthlySalary(monthlySalary);
            employeeService.saveEmployee(e);
        });
        return "redirect:/finance/candidates";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        List<Employee> list = employeeService.getAllEmployees().stream()
                .filter(e -> Boolean.TRUE.equals(e.getInSalarySystem()))
                .collect(Collectors.toList());
        model.addAttribute("employees", list);
        return "finance-employees";
    }

    @PostMapping("/employees/{id}/salary")
    public String updateMonthlySalary(@PathVariable Long id, @RequestParam Double monthlySalary) {
        employeeService.getEmployeeById(id).ifPresent(e -> {
            e.setMonthlySalary(monthlySalary);
            employeeService.saveEmployee(e);
        });
        return "redirect:/finance/employees";
    }

    @GetMapping("/process")
    public String processForm(@RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month,
                              Model model) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();

        List<Employee> inSalary = employeeService.getAllEmployees().stream()
                .filter(e -> Boolean.TRUE.equals(e.getInSalarySystem()))
                .collect(Collectors.toList());
        List<StaffPayment> monthPayments = staffPaymentService.getPaymentsByYearMonth(y, m);
        java.util.Set<Long> paidIds = monthPayments.stream()
                .filter(p -> "Completed".equalsIgnoreCase(p.getStatus()))
                .map(p -> p.getEmployee() != null ? p.getEmployee().getId() : null)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        model.addAttribute("employees", inSalary);
        model.addAttribute("paidIds", paidIds);
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        return "salary-process";
    }

    @PostMapping("/process")
    public String process(@RequestParam Long employeeId,
                          @RequestParam Integer year,
                          @RequestParam Integer month) {
        StaffPayment p = new StaffPayment();
        p.setEmployee(employeeService.getEmployeeById(employeeId).orElse(null));
        p.setAmountPaid(employeeService.getEmployeeById(employeeId).map(Employee::getMonthlySalary).orElse(0.0));
        p.setPaymentYear(year);
        p.setPaymentMonth(month);
        p.setPaymentDate(new Date());
        p.setPaymentMethod("bankTransfer");
        p.setStatus("Completed");
        staffPaymentService.addStaffPayment(p);
        return "redirect:/finance/process?year=" + year + "&month=" + month;
    }

    @PostMapping("/process-bulk")
    public String processBulk(@RequestParam Integer year,
                              @RequestParam Integer month,
                              @RequestParam(name = "employeeIds", required = false) List<Long> employeeIds) {
        if (employeeIds != null) {
            for (Long employeeId : employeeIds) {
                StaffPayment p = new StaffPayment();
                p.setEmployee(employeeService.getEmployeeById(employeeId).orElse(null));
                p.setAmountPaid(employeeService.getEmployeeById(employeeId).map(Employee::getMonthlySalary).orElse(0.0));
                p.setPaymentYear(year);
                p.setPaymentMonth(month);
                p.setPaymentDate(new Date());
                p.setPaymentMethod("bankTransfer");
                p.setStatus("Completed");
                staffPaymentService.addStaffPayment(p);
            }
        }
        return "redirect:/finance/process?year=" + year + "&month=" + month;
    }

    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestParam Long invoiceId) {
        invoiceService.markPaid(invoiceId);
        // If an order is linked by invoice id == order id (placeholder), mark payment confirmed
        try { orderService.confirmPayment(invoiceId); } catch (Exception ignored) {}
        return "redirect:/finance/dashboard";
    }
}
