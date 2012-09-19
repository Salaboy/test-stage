/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import org.drools.KnowledgeBase;
import org.drools.container.spring.beans.persistence.DroolsSpringJpaManager;
import org.drools.container.spring.beans.persistence.DroolsSpringTransactionManager;
import org.drools.impl.EnvironmentFactory;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jbpm.task.api.TaskServiceEntryPoint;
import org.jbpm.task.wih.CDIHTWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 *
 * @author salaboy
 */
@RunWith(Arquillian.class)
public class CDISpringIntegrationTest {

    @Deployment()
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "env-conf-standalone.jar")
                .addPackage("org.jboss.seam.persistence") //seam-persistence
                .addPackage("org.jboss.seam.transaction") //seam-persistence
                .addPackage("org.jbpm")
                .addPackage("org.jbpm.task")
                .addPackage("org.jbpm.task.wih") // work items
                .addPackage("org.jbpm.task.annotations")
                .addPackage("org.jbpm.task.api")
                .addPackage("org.jbpm.task.impl")
                .addPackage("org.jbpm.task.events")
                .addPackage("org.jbpm.task.exception")
                .addPackage("org.jbpm.task.identity")
                .addPackage("org.jbpm.task.factories")
                .addPackage("org.jbpm.task.internals")
                .addPackage("org.jbpm.task.internals.lifecycle")
                .addPackage("org.jbpm.task.lifecycle.listeners")
                .addPackage("org.jbpm.task.query")
                .addPackage("org.jbpm.task.util")
                .addPackage("org.jbpm.task.commands") // This should not be required here
                .addPackage("org.jbpm.task.deadlines") // deadlines
                .addPackage("org.jbpm.task.deadlines.notifications.impl")
                .addPackage("org.jbpm.task.subtask")
                .addAsResource("spring-conf.xml", ArchivePaths.create("spring-conf.xml"))
                .addAsResource("sample.bpmn", ArchivePaths.create("sample.bpmn"))
                .addAsManifestResource("META-INF/persistence.xml", ArchivePaths.create("persistence.xml"))
                .addAsManifestResource("META-INF/Taskorm.xml", ArchivePaths.create("Taskorm.xml"))
                .addAsManifestResource("META-INF/beans.xml", ArchivePaths.create("beans.xml"));

    }
    @Inject
    protected TaskServiceEntryPoint taskService;
    @Inject
    private CDIHTWorkItemHandler htWorkItemHandler;
    
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        int removeAllTasks = taskService.removeAllTasks();

    }

    @Test
    public void hello() {
        
        ClassPathXmlApplicationContext context =
                    new ClassPathXmlApplicationContext("spring-conf.xml");
        
        KnowledgeBase kbase = (KnowledgeBase) context.getBean("kbase");
        
        EntityManagerFactory emf = (EntityManagerFactory) context.getBean("jbpmEMF");

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean("jbpmTxManager");
        TransactionManager transactionManager = new DroolsSpringTransactionManager(aptm);
        env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);

        PersistenceContextManager persistenceContextManager = new DroolsSpringJpaManager(env);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);


        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        htWorkItemHandler.setSession(ksession);
        htWorkItemHandler.init();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htWorkItemHandler);



    }
}
