package com.perscholas.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton wrapper for the JPA {@link EntityManagerFactory}.
 *
 * <p>Call {@link #getEntityManagerFactory()} to obtain the factory,
 * and {@link #shutdown()} when the application exits.</p>
 */
public class JPAUtil {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("classicmodels");

    private JPAUtil() { }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static void shutdown() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
