package com.agentflow.workflow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    private final WorkflowRepository workflows;
    private final WorkflowExecutionRepository executions;

    public WorkflowController(WorkflowRepository workflows, WorkflowExecutionRepository executions) {
        this.workflows = workflows;
        this.executions = executions;
    }

    public record CreateWorkflow(@NotBlank @Size(max = 120) String name,
                                 @Size(max = 1000) String description) {}

    public record WorkflowDto(UUID id, String name, String description, String status) {
        static WorkflowDto from(Workflow workflow) {
            return new WorkflowDto(workflow.getId(), workflow.getName(),
                    workflow.getDescription(), workflow.getStatus());
        }
    }

    public record ExecutionDto(UUID id, UUID workflowId, String status,
                               Instant createdAt, Instant startedAt, Instant finishedAt) {
        static ExecutionDto from(WorkflowExecution execution) {
            return new ExecutionDto(execution.getId(), execution.getWorkflowId(),
                    execution.getStatus(), execution.getCreatedAt(),
                    execution.getStartedAt(), execution.getFinishedAt());
        }
    }

    @GetMapping
    public List<WorkflowDto> list() {
        return workflows.findAll().stream().map(WorkflowDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowDto create(@Valid @RequestBody CreateWorkflow request) {
        return WorkflowDto.from(workflows.save(new Workflow(request.name(), request.description())));
    }

    @PostMapping("/{id}/executions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ExecutionDto execute(@PathVariable UUID id) {
        Workflow workflow = workflows.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
        return ExecutionDto.from(executions.save(new WorkflowExecution(workflow)));
    }

    @GetMapping("/{id}/executions")
    @Transactional(readOnly = true)
    public List<ExecutionDto> executions(@PathVariable UUID id) {
        if (!workflows.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found");
        }
        return executions.findByWorkflowIdOrderByCreatedAtDesc(id).stream()
                .map(ExecutionDto::from).toList();
    }

    @PostMapping("/executions/{id}/start")
    @Transactional
    public ExecutionDto start(@PathVariable UUID id) {
        WorkflowExecution execution = getExecution(id);
        try {
            execution.start();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return ExecutionDto.from(execution);
    }

    @PostMapping("/executions/{id}/complete")
    @Transactional
    public ExecutionDto complete(@PathVariable UUID id) {
        WorkflowExecution execution = getExecution(id);
        try {
            execution.complete();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return ExecutionDto.from(execution);
    }

    private WorkflowExecution getExecution(UUID id) {
        return executions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Execution not found"));
    }
}
