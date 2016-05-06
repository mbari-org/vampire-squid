package org.mbari.vars.vam.dao.jpa;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistence service implementation for use in Java SE environments. Basically,
 * it's a decorator for EntityManager
 * Use as:
 * <code>
 * DAO dao = new DAO(entityManager);
 * dao.startTransaction();
 * // Call whatever dao methods you need
 * dao.endTransaction();
 * </code>
 */
public class DAO {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final String JDBC_URL_KEY = "javax.persistence.jdbc.url";

    /**
     *
     */
    public enum TransactionType {
        REMOVE, FIND, PERSIST, LOAD_LAZY_RELATIONS,    // <-- Load those lazy relationships
        MERGE
    }


}
