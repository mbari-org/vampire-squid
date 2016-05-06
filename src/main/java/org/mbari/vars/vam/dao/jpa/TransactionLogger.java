package org.mbari.vars.vam.dao.jpa;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brian
 */
public class TransactionLogger {

    private final Logger log = LoggerFactory.getLogger(TransactionLogger.class);

    public TransactionLogger() {
    }

    @PostLoad
    public void logLoad(Object object) {
        if (log.isDebugEnabled()) {
            log.debug("Loaded '{}' into persistent context", object);
        }
    }

    @PrePersist
    public void logPersist(Object object) {
        logTransaction(object, TransactionType.PERSIST);
    }

    @PreRemove
    public void logRemove(Object object) {
        logTransaction(object, TransactionType.REMOVE);
    }

    @PreUpdate
    public void logUpdate(Object object) {
        logTransaction(object, TransactionType.MERGE);
    }

    private void logTransaction(Object object, TransactionType transactionType) {
        if (log.isDebugEnabled()) {
            log.debug("Performing '{}' on {}", transactionType, object);
        }
    }
}