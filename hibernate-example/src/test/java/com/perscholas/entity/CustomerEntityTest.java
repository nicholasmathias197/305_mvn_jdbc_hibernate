package com.perscholas.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link Customer} entity using H2 in-memory database.
 *
 * <p>Uses the {@code classicmodels-test} persistence unit defined in
 * {@code src/test/resources/META-INF/persistence.xml}.</p>
 */
class CustomerEntityTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("classicmodels-test");
    }

    @AfterAll
    static void tearDownFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) em.close();
    }

    @Test
    @DisplayName("Persist and find a Customer")
    void persistAndFind() {
        em.getTransaction().begin();

        Customer c = new Customer();
        c.setCustomerNumber(1);
        c.setCustomerName("Test Customer");
        c.setContactLastName("Smith");
        c.setContactFirstName("John");
        c.setPhone("555-1234");
        c.setAddressLine1("123 Main St");
        c.setCity("TestCity");
        c.setCountry("USA");
        c.setCreditLimit(new BigDecimal("10000.00"));

        em.persist(c);
        em.getTransaction().commit();

        em.clear(); // force a fresh read from H2

        Customer found = em.find(Customer.class, 1);
        assertNotNull(found);
        assertEquals("Test Customer", found.getCustomerName());
        assertEquals("USA", found.getCountry());
    }

    @Test
    @DisplayName("Update a Customer")
    void updateCustomer() {
        em.getTransaction().begin();

        Customer c = new Customer();
        c.setCustomerNumber(2);
        c.setCustomerName("Original Name");
        c.setContactLastName("Doe");
        c.setContactFirstName("Jane");
        c.setPhone("555-5678");
        c.setAddressLine1("456 Elm St");
        c.setCity("Updateville");
        c.setCountry("Canada");
        em.persist(c);
        em.getTransaction().commit();

        em.getTransaction().begin();
        Customer toUpdate = em.find(Customer.class, 2);
        toUpdate.setCustomerName("Updated Name");
        em.merge(toUpdate);
        em.getTransaction().commit();

        em.clear();
        Customer updated = em.find(Customer.class, 2);
        assertEquals("Updated Name", updated.getCustomerName());
    }

    @Test
    @DisplayName("Delete a Customer")
    void deleteCustomer() {
        em.getTransaction().begin();

        Customer c = new Customer();
        c.setCustomerNumber(3);
        c.setCustomerName("To Be Deleted");
        c.setContactLastName("Gone");
        c.setContactFirstName("Soon");
        c.setPhone("555-0000");
        c.setAddressLine1("789 Oak Ave");
        c.setCity("Deletetown");
        c.setCountry("UK");
        em.persist(c);
        em.getTransaction().commit();

        em.getTransaction().begin();
        Customer toDelete = em.find(Customer.class, 3);
        em.remove(toDelete);
        em.getTransaction().commit();

        assertNull(em.find(Customer.class, 3));
    }

    @Test
    @DisplayName("JPQL query by country")
    void jpqlQueryByCountry() {
        em.getTransaction().begin();

        for (int i = 10; i < 13; i++) {
            Customer c = new Customer();
            c.setCustomerNumber(i);
            c.setCustomerName("FR Customer " + i);
            c.setContactLastName("Last" + i);
            c.setContactFirstName("First" + i);
            c.setPhone("555-" + i);
            c.setAddressLine1(i + " Rue de Test");
            c.setCity("Paris");
            c.setCountry("France");
            em.persist(c);
        }
        em.getTransaction().commit();

        List<Customer> french = em.createQuery(
                "SELECT c FROM Customer c WHERE c.country = :country", Customer.class)
                .setParameter("country", "France")
                .getResultList();

        assertEquals(3, french.size());
        french.forEach(c -> assertEquals("France", c.getCountry()));
    }
}
