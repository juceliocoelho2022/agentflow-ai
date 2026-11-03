package com.agentflow.workflow;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="workflows")
public class Workflow {
 @Id private UUID id;
 @Column(nullable=false,length=120) private String name;
 @Column(length=1000) private String description;
 @Column(nullable=false,length=30) private String status;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 protected Workflow() {}
 public Workflow(String name,String description) { this.id=UUID.randomUUID();this.name=name;this.description=description;this.status="ACTIVE";this.createdAt=Instant.now(); }
 public UUID getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
