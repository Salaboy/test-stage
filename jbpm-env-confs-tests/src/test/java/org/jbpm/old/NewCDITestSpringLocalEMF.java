package org.jbpm.old;

import java.util.List;

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
import org.drools.runtime.process.ProcessInstance;
import org.h2.tools.Server;
import org.jbpm.task.api.TaskServiceEntryPoint;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.utils.TaskServiceModule;
import org.jbpm.task.wih.GenericHTWorkItemHandler;

import org.junit.Test;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This is a sample file to launch a process.
 * 
 * 
 */


public class NewCDITestSpringLocalEMF {

    @Test
    public void hello() {
        
        
        try {
            // start h2 in memory database
            Server server = Server.createTcpServer(new String[0]);
            server.start();

        } catch (Throwable t) {
            throw new RuntimeException("Could not start H2 server", t);
        }

        BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
        BeanFactoryReference bf = bfl.useBeanFactory("org.jbpm");
       

        EntityManagerFactory emf = (EntityManagerFactory) bf.getFactory().getBean("springEntityManagerFactory");

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) bf.getFactory().getBean("jbpmTxManager");
        TransactionManager transactionManager = new DroolsSpringTransactionManager(aptm);
        env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);

        PersistenceContextManager persistenceContextManager = new DroolsSpringJpaManager(env);
        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);

        KnowledgeBase kbase = (KnowledgeBase) bf.getFactory().getBean("kbase");
        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        // TODO: new JPAWorkingMemoryDbLogger(ksession);

//        org.jbpm.task.service.TaskService internalTaskService = new org.jbpm.task.service.TaskService();
//        internalTaskService.setSystemEventListener(SystemEventListenerFactory.getSystemEventListener());
//        TaskSessionSpringFactoryImpl springFactory = new TaskSessionSpringFactoryImpl();
//        springFactory.setEntityManagerFactory(emf);
//        springFactory.setTaskService(internalTaskService);
//        springFactory.setTransactionManager(transactionManager);
//        springFactory.setUseJTA(true);
//        springFactory.initialize();
//        TaskService taskService = new LocalTaskService(internalTaskService);
//
//        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(taskService, ksession);
//        humanTaskHandler.setLocal(true);
//        humanTaskHandler.connect();
        
        TaskServiceEntryPoint taskService = new TaskServiceModule().getTaskService();
        GenericHTWorkItemHandler htWorkItemHandler = new GenericHTWorkItemHandler(taskService, ksession);
        htWorkItemHandler.init();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htWorkItemHandler);

//        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");
//
//        System.out.println("Process started");
//
//        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
//        System.out.println("Found " + tasks.size() + " task(s) for user 'john'");
//        if (tasks.size() != 1) {
//            throw new IllegalArgumentException("Incorrect amount of tasks");
//        }
//        long taskId = tasks.get(0).getId();
//        taskService.start(taskId, "john");
//        taskService.complete(taskId, "john", null);
//
//        tasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
//        System.out.println("Found " + tasks.size() + " task(s) for user 'mary'");
//        if (tasks.size() != 1) {
//            throw new IllegalArgumentException("Incorrect amount of tasks");
//        }
//        taskId = tasks.get(0).getId();
//        taskService.start(taskId, "mary");
//        taskService.complete(taskId, "mary", null);
//
//        processInstance = ksession.getProcessInstance(processInstance.getId());
//        if (processInstance == null) {
//            System.out.println("Process instance completed");
//        }
//
//        System.out.println("******************************************************************");

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = aptm.getTransaction(def);
        
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        aptm.rollback(status);

        processInstance = ksession.getProcessInstance(processInstance.getId());

        if (processInstance == null) {
            System.out.println("Process instance rolled back");
        } else {
            throw new IllegalArgumentException("Process instance not rolled back");
        }

        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'john'");
        if (tasks.size() != 0) {
            throw new IllegalArgumentException("Incorrect amount of tasks");
        }

    }
}