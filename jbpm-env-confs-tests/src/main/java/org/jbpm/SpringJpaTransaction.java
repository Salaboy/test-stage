package org.jbpm;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.seam.transaction.AbstractUserTransaction;
import org.jboss.seam.transaction.DefaultTransaction;
import org.jboss.seam.transaction.Synchronizations;
import org.jboss.solder.core.Veto;
import org.jboss.solder.logging.Logger;
import org.jboss.weld.environment.se.contexts.ThreadScoped;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Marius Bogoevici
 */
@ApplicationScoped
@DefaultTransaction
@Veto
public class SpringJpaTransaction extends AbstractUserTransaction {

    private static final Logger log = Logger.getLogger(SpringJpaTransaction.class);

    @Inject
    JpaTransactionManager jpaTransactionManager;

    public static TransactionDefinition TRANSACTION_DEFINITION = new DefaultTransactionDefinition();
    private TransactionStatus currentTransaction;


    @Inject
    public void init(Synchronizations sync) {
        setSynchronizations(sync);
    }

    public SpringJpaTransaction() {
    }

    public void begin() throws NotSupportedException, SystemException {
        log.debug("beginning JPA resource-local transaction");
        // TODO: translate exceptions that occur into the correct JTA exception
        try {
            currentTransaction = jpaTransactionManager.getTransaction(TRANSACTION_DEFINITION);
            getSynchronizations().afterTransactionBegin();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        log.debug("committing Spring resource-local transaction");
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No Spring transaction is currently active");
        }
        boolean success = false;
        try {

                getSynchronizations().beforeTransactionCommit();
                jpaTransactionManager.commit(currentTransaction);
                success = true;

        } finally {
            currentTransaction = null;
            getSynchronizations().afterTransactionCompletion(success);
        }
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        log.debug("rolling back Spring resource-local transaction");
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No Spring transaction is currently active");
        }
        boolean success = false;
        try {

            getSynchronizations().beforeTransactionCommit();
            jpaTransactionManager.rollback(currentTransaction);


        } finally {
            currentTransaction = null;
            getSynchronizations().afterTransactionCompletion(success);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
        {
            throw new IllegalStateException("No Spring Transaction is currently available.");
        }
        TransactionStatus transaction = null;
        PlatformTransactionManager ptm = jpaTransactionManager;
        try
        {
            if (currentTransaction == null)
            {
                transaction = ptm.getTransaction(TRANSACTION_DEFINITION);
            }
            else
            {
                transaction = currentTransaction;
            }
            transaction.setRollbackOnly();
        }
        finally
        {
            if (currentTransaction == null)
            {
                ptm.commit(transaction);
            }
        }

    }

    public int getStatus() throws SystemException {
        PlatformTransactionManager ptm = jpaTransactionManager;
        if (ptm == null)
        {
            return Status.STATUS_NO_TRANSACTION;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive())
        {
            TransactionStatus transaction = null;
            try
            {
                if (currentTransaction == null)
                {
                    transaction = ptm.getTransaction(TRANSACTION_DEFINITION);
                    if (transaction.isNewTransaction())
                    {
                        return Status.STATUS_COMMITTED;
                    }
                }
                else
                {
                    transaction = currentTransaction;
                }
                // If SynchronizationManager thinks it has an active transaction but
                // our transaction is a new one
                // then we must be in the middle of committing
                if (transaction.isCompleted())
                {
                    if (transaction.isRollbackOnly())
                    {
                        return Status.STATUS_ROLLEDBACK;
                    }
                    return Status.STATUS_COMMITTED;
                }
                else
                {
                    if (transaction.isRollbackOnly())
                    {
                        return Status.STATUS_MARKED_ROLLBACK;
                    }
                    return Status.STATUS_ACTIVE;
                }
            }
            finally
            {
                if (currentTransaction == null)
                {
                    ptm.commit(transaction);
                }
            }
        }
        return Status.STATUS_NO_TRANSACTION;

    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerSynchronization(Synchronization sync) {
        if (log.isDebugEnabled()) {
            log.debug("registering synchronization: " + sync);
        }
        // try to register the synchronization directly with the
        // persistence provider
        getSynchronizations().registerSynchronization(sync);
    }

    @Override
    public boolean isConversationContextRequired() {
        return true;
    }

    @Override
    public void enlist(EntityManager entityManager) {
        // no-op
    }
}
