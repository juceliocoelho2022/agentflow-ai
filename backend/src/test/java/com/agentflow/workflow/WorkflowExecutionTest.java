package com.agentflow.workflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowExecutionTest {
    @Test
    void newExecutionMustRemainPendingWithoutStartTime() {
        var execution = new WorkflowExecution(new Workflow("Test", "Flow"));
        assertEquals("PENDING", execution.getStatus());
        assertNotNull(execution.getCreatedAt());
        assertNull(execution.getStartedAt());
        assertNull(execution.getFinishedAt());
    }

    @Test
    void mustEnforceStateTransitionsAndTimestamps() {
        var execution = new WorkflowExecution(new Workflow("Test", "Flow"));
        assertThrows(IllegalStateException.class, execution::complete);

        execution.start();
        assertEquals("RUNNING", execution.getStatus());
        assertNotNull(execution.getStartedAt());
        assertNull(execution.getFinishedAt());

        execution.complete();
        assertEquals("COMPLETED", execution.getStatus());
        assertNotNull(execution.getFinishedAt());
        assertThrows(IllegalStateException.class, execution::start);
        assertThrows(IllegalStateException.class, execution::complete);
    }

    @Test
    void startingTwiceMustBeRejected() {
        var execution = new WorkflowExecution(new Workflow("Test", "Flow"));
        execution.start();
        assertThrows(IllegalStateException.class, execution::start);
    }
}
