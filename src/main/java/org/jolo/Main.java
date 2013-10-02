package org.jolo;


import org.activiti.engine.*;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.List;

public class Main {

    public static ProcessInstance processInstance;
    public static final String SIGNAL = "launchSubprocessSignal";
    public static final String MAIN_PROCESS = "mainProcess";
    public static Integer subProcessToken = 0;

    /**
     * Activiti services
     */
    private static ClassPathXmlApplicationContext applicationContext;
    private static RuntimeService runtimeService;
    private static TaskService taskService;

    static {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        runtimeService = (RuntimeService) applicationContext.getBean("runtimeService");
        taskService = (TaskService) applicationContext.getBean("taskService");
    }

    public static void main(String[] args) throws Exception {
        initializeProcess();

        //Waiting to check if subprocesses are asynchronous
        Thread.currentThread().sleep(10000);

        chooseChampion();

        finishProcess();

        //wait to get chance for all processes to finish
        Thread.currentThread().sleep(1000);

        if (checkForZombie())
            throw new IllegalStateException("There shouldn't be any active executions");
    }

    /**
     * Launches one main process and two async subprocesses
     */
    private static void initializeProcess() {
        //launch main process
        processInstance = runtimeService.startProcessInstanceByKey(MAIN_PROCESS);

        //launch asynchronous subprocess of the main process
        Execution execution = runtimeService.createExecutionQuery()
                .signalEventSubscriptionName(SIGNAL)
                .processInstanceId(processInstance.getId())
                .singleResult();

        //start two asyncronous subprocesses
        runtimeService.signalEventReceived(SIGNAL, execution.getId());
        runtimeService.signalEventReceived(SIGNAL, execution.getId());
    }

    /**
     * Choose champion subprocess and complete it
     */
    private static void chooseChampion() {
        //complete process for champion candidate
        log("SEARCHING FOR CHAMPION CANDIDATE...");
        runtimeService.setVariable(processInstance.getId(), "isChampion", true);
        List<Execution> mainProcExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getProcessInstanceId()).activityId("subUserTask").list();
        for (Execution ex : mainProcExecutions) {
            if (subProcessToken.equals(runtimeService.getVariableLocal(ex.getId(), "profileId"))) {
                log("FIND CHAMPION SUBPROCESS");
                Task champUserTask = taskService.createTaskQuery().executionId(ex.getId()).singleResult();
                taskService.complete(champUserTask.getId());
            }
        }
    }

    private static void finishProcess() {
        proceedMainProcess();
        proceedSubProcesses();
    }

    private static void proceedMainProcess() {
        Task chooseChampionTask = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).taskDefinitionKey("chooseChampionTask").singleResult();
        taskService.complete(chooseChampionTask.getId());
    }

    /**
     * Proceed continuation of 'looser' subprocesses.
     */
    private static void proceedSubProcesses() {
        log("CONTINUE LOOSER'S SUBPROCESSES");
        runtimeService.setVariable(processInstance.getId(), "isChampion", false);
        List<Execution> loosers = runtimeService.createExecutionQuery().processInstanceId(processInstance.getProcessInstanceId()).activityId("subUserTask").list();
        for (Execution ex : loosers) {
            Task task = taskService.createTaskQuery().executionId(ex.getId()).singleResult();
            taskService.complete(task.getId());
        }
    }

    /**
     * Check if there are zombie executions
     * @return true if there are active executions
     */
    private static boolean checkForZombie() {
        return !runtimeService.createExecutionQuery()
                              .processInstanceId(processInstance
                                                  .getProcessInstanceId())
                              .list()
                              .isEmpty();
    }

    public static synchronized int tokenIncAndGet() {
        return ++subProcessToken;
    }

    public synchronized static void log(String msg) {
        System.out.println(msg);
    }
}
