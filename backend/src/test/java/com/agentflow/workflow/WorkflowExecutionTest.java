package com.agentflow.workflow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WorkflowExecutionTest {
 @Test void mustEnforceTransitions(){var e=new WorkflowExecution(new Workflow("Test","Flow"));assertEquals("PENDING",e.getStatus());assertThrows(IllegalStateException.class,e::complete);e.start();e.complete();assertEquals("COMPLETED",e.getStatus());assertThrows(IllegalStateException.class,e::start);}
}
