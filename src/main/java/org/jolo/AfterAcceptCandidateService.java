package org.jolo;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import static org.jolo.Main.log;

/**
 * Created with IntelliJ IDEA.
 * User: v.bakhmatiuk
 * Date: 10/2/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class AfterAcceptCandidateService implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String executionToken = execution.getCurrentActivityName() + ":" + execution.getParentId();
        log(executionToken + " service is started...");
    }
}
