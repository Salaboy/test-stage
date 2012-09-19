/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import org.jboss.seam.spring.context.Configuration;
import org.jboss.seam.spring.context.SpringContext;
import org.jboss.seam.spring.inject.SpringBean;
import org.jboss.solder.core.ExtensionManaged;
import org.jboss.weld.context.ApplicationContext;

/**
 *
 * @author salaboy
 */
public class SpringContextProducer {

    @Produces
    @SpringContext
    @Configuration(locations = "classpath*:spring-conf.xml")
    private ApplicationContext context;
   
}
