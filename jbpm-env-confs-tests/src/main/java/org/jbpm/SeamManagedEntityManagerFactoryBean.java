/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import org.jboss.seam.persistence.util.BeanManagerUtils;
import org.jboss.solder.core.Veto;
import org.jbpm.task.utils.TaskServiceModule;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

/**
 *
 * @author salaboy
 */
@Veto
public class SeamManagedEntityManagerFactoryBean extends
                AbstractEntityManagerFactoryBean {
        private EntityManagerFactory entityManagerFactory;
        private String persistenceContextName;

        @Override
        protected EntityManagerFactory createNativeEntityManagerFactory()
                        throws PersistenceException {
                return getDelegate();
        }

        @Override
        public String getPersistenceUnitName() {
                String persistenceUnitName = super.getPersistenceUnitName();
                if (persistenceUnitName == null || "".equals(persistenceUnitName)) {
                        return persistenceContextName;
                }
                return persistenceUnitName;
        }

        public void setPersistenceContextName(String persistenceContextName) {
                this.persistenceContextName = persistenceContextName;
        }

        private EntityManagerFactory getDelegate() {
                if (entityManagerFactory == null) {
                        initEntityManagerFactory();
                }
                return entityManagerFactory;
        }

        private void initEntityManagerFactory() {
              entityManagerFactory = BeanManagerUtils.getContextualInstance(
                                        TaskServiceModule.getInstance().getContainer().getBeanManager(), EntityManagerFactory.class);
        }
}
