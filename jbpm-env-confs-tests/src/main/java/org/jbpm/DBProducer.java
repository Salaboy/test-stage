/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm;

import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManagerFactory;
import org.jboss.solder.core.ExtensionManaged;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.web.context.ContextLoader;


/**
 *
 * @author salaboy
 */
public class DBProducer {
//    @Inject @SpringBean 
//    private EntityManagerFactory jbpmEMF;
//    
//    @TaskPersistence
//    @ExtensionManaged
//    @ApplicationScoped
//    @Produces 
//    private EntityManagerFactory emf;

    @ExtensionManaged
    @Produces
    @ApplicationScoped
    @Default
    public EntityManagerFactory getEntityManagerFactory() {
        BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
        BeanFactoryReference bf = bfl.useBeanFactory("org.jbpm");
        return (EntityManagerFactory) bf.getFactory().getBean("springEntityManagerFactory");
    }

    @Produces
    public Logger createLogger(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember()
                .getDeclaringClass().getName());
    }
}
