CREATE TABLE workflows (
 id UUID PRIMARY KEY, name VARCHAR(120) NOT NULL, description VARCHAR(1000),
 status VARCHAR(30) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE workflow_executions (
 id UUID PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES workflows(id),
 status VARCHAR(30) NOT NULL, started_at TIMESTAMPTZ NOT NULL, finished_at TIMESTAMPTZ
);
CREATE INDEX idx_execution_workflow ON workflow_executions(workflow_id);
