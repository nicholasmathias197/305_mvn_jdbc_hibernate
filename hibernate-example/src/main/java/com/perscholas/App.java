package com.perscholas;

import com.perscholas.entity.Customer;
import com.perscholas.entity.Employee;
import com.perscholas.entity.Product;
import com.perscholas.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entry point demonstrating Hibernate / JPA operations against classicmodels.
 *
 * <p>Prerequisites:
 * <ol>
 *   <li>MySQL running on localhost:3306</li>
 *   <li>classicmodels database loaded (run classicmodels.sql)</li>
 *   <li>Update persistence.xml if credentials differ from root/password</li>
 * </ol>
 */
public class App {

    public static void main(String[] args) {

        // ---- READ: find by primary key ----
        System.out.println("=== Find Customer by ID ===");
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        Customer customer = em.find(Customer.class, 103);
        System.out.println(customer);
        em.close();

        // ---- READ: JPQL query ----
        System.out.println("\n=== Customers in France (JPQL) ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.country = :country ORDER BY c.customerName",
                Customer.class);
        query.setParameter("country", "France");
        List<Customer> frenchCustomers = query.getResultList();
        frenchCustomers.forEach(System.out::println);
        em.close();

        // ---- CREATE: persist a new customer ----
        System.out.println("\n=== Insert New Customer ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();

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
        em.persist(newCustomer);

        em.getTransaction().commit();
        System.out.println("Persisted: " + newCustomer);
        em.close();

        // ---- UPDATE: merge changes ----
        System.out.println("\n=== Update Customer ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();

        Customer toUpdate = em.find(Customer.class, 900);
        toUpdate.setCustomerName("Per Scholas Updated Store");
        toUpdate.setCreditLimit(new BigDecimal("75000.00"));
        em.merge(toUpdate);

        em.getTransaction().commit();
        System.out.println("Updated: " + toUpdate);
        em.close();

        // ---- DELETE: remove entity ----
        System.out.println("\n=== Delete Customer ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();

        Customer toDelete = em.find(Customer.class, 900);
        if (toDelete != null) {
            em.remove(toDelete);
            System.out.println("Deleted customer 900");
        }

        em.getTransaction().commit();
        em.close();

        // ---- Navigating relationships ----
        System.out.println("\n=== Employee → Office relationship ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        Employee emp = em.find(Employee.class, 1002);
        System.out.println(emp.getFirstName() + " " + emp.getLastName()
                + " works in " + emp.getOffice().getCity());
        em.close();

        // ---- HQL aggregate query ----
        System.out.println("\n=== Product count by product line ===");
        em = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Object[]> results = em.createQuery(
                "SELECT p.productLine.productLine, COUNT(p) " +
                "FROM Product p GROUP BY p.productLine.productLine " +
                "ORDER BY COUNT(p) DESC", Object[].class)
                .getResultList();
        for (Object[] row : results) {
            System.out.printf("  %-20s %d%n", row[0], row[1]);
        }
        em.close();

        // ---- Shutdown ----
        JPAUtil.shutdown();
        System.out.println("\nDone.");
    }
}
