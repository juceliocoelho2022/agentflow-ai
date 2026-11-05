package com.agentflow.workflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    protected WorkflowExecution() {}

    public WorkflowExecution(Workflow workflow) {
        this.id = UUID.randomUUID();
        this.workflow = workflow;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public void start() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException("Only PENDING executions can start");
        }
        this.status = "RUNNING";
        this.startedAt = Instant.now();
    }

    public void complete() {
        if (!"RUNNING".equals(status)) {
            throw new IllegalStateException("Only RUNNING executions can complete");
        }
        this.status = "COMPLETED";
        this.finishedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkflowId() { return workflow.getId(); }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
}
