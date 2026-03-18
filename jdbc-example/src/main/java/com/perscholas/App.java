package com.perscholas;

import com.perscholas.dao.*;
import com.perscholas.model.Customer;
import com.perscholas.model.Employee;
import com.perscholas.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Entry point that demonstrates JDBC + DAO CRUD operations against the classicmodels database.
 *
 * <p>Prerequisites:
 * <ol>
 *   <li>MySQL is running on localhost:3306</li>
 *   <li>classicmodels database has been loaded (run classicmodels.sql)</li>
 *   <li>Update ConnectionManager if your credentials differ from root/password</li>
 * </ol>
 */
public class App {

    public static void main(String[] args) {

        // ---- Customer DAO demo ----
        CustomerDAO customerDAO = new CustomerDAOImpl();

        System.out.println("=== Find Customer by ID ===");
        Optional<Customer> found = customerDAO.findById(103);
        found.ifPresent(System.out::println);

        System.out.println("\n=== Customers in France ===");
        List<Customer> frenchCustomers = customerDAO.findByCountry("France");
        frenchCustomers.forEach(System.out::println);

        System.out.println("\n=== Insert New Customer ===");
        Customer newCustomer = new Customer();
        newCustomer.setCustomerNumber(900);
        newCustomer.setCustomerName("Per Scholas Demo Store");
        newCustomer.setContactLastName("Doe");
        newCustomer.setContactFirstName("Jane");
        newCustomer.setPhone("555-0199");
        newCustomer.setAddressLine1("100 Learning Lane");
        newCustomer.setCity("New York");
        newCustomer.setCountry("USA");
        newCustomer.setCreditLimit(new BigDecimal("50000.00"));
        customerDAO.insert(newCustomer);
        System.out.println("Inserted: " + newCustomer);

        System.out.println("\n=== Update Customer ===");
        newCustomer.setCustomerName("Per Scholas Updated Store");
        newCustomer.setCreditLimit(new BigDecimal("75000.00"));
        boolean updated = customerDAO.update(newCustomer);
        System.out.println("Updated: " + updated);

        System.out.println("\n=== Delete Customer ===");
        boolean deleted = customerDAO.delete(900);
        System.out.println("Deleted: " + deleted);

        // ---- Employee DAO demo ----
        EmployeeDAO employeeDAO = new EmployeeDAOImpl();

        System.out.println("\n=== All Employees ===");
        List<Employee> employees = employeeDAO.findAll();
        employees.forEach(System.out::println);

        System.out.println("\n=== Sales Reps ===");
        List<Employee> salesReps = employeeDAO.findByJobTitle("Sales Rep");
        System.out.println("Count: " + salesReps.size());

        // ---- Product DAO demo ----
        ProductDAO productDAO = new ProductDAOImpl();

        System.out.println("\n=== Motorcycles ===");
        List<Product> motorcycles = productDAO.findByProductLine("Motorcycles");
        motorcycles.forEach(System.out::println);

        System.out.println("\n=== Find Product by Code ===");
        Optional<Product> product = productDAO.findById("S10_1678");
        product.ifPresent(p -> System.out.println(p.getProductName() + " — MSRP $" + p.getMsrp()));

        System.out.println("\nDone.");
    }
}
