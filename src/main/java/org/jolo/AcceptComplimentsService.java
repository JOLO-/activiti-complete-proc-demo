package org.jolo;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import static org.jolo.Main.log;

public class AcceptComplimentsService implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String executionToken = execution.getCurrentActivityName() + ":"  + execution.getParentId();
        String reaction = ((Boolean) execution.getVariable("isChampion")) ? "> \"I'm really the best\" (c)" : "> \"You'll regret about your choice\" (c)";
        log(executionToken + reaction);
    }
}
