package com.agentflow.workflow;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="workflow_executions")
public class WorkflowExecution {
 @Id private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="workflow_id") private Workflow workflow;
 @Column(nullable=false,length=30) private String status;
 @Column(name="started_at",nullable=false) private Instant startedAt;
 @Column(name="finished_at") private Instant finishedAt;
 protected WorkflowExecution() {}
 public WorkflowExecution(Workflow workflow) {this.id=UUID.randomUUID();this.workflow=workflow;this.status="PENDING";this.startedAt=Instant.now();}
 public void start(){if(!status.equals("PENDING"))throw new IllegalStateException("Only PENDING executions can start");status="RUNNING";}
 public void complete(){if(!status.equals("RUNNING"))throw new IllegalStateException("Only RUNNING executions can complete");status="COMPLETED";finishedAt=Instant.now();}
 public UUID getId(){return id;} public UUID getWorkflowId(){return workflow.getId();} public String getStatus(){return status;} public Instant getStartedAt(){return startedAt;} public Instant getFinishedAt(){return finishedAt;}
}
