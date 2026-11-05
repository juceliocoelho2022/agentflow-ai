-- Separa criação da execução e início efetivo, preservando a informação legada.
ALTER TABLE workflow_executions ADD COLUMN created_at TIMESTAMPTZ;
UPDATE workflow_executions SET created_at = started_at;
ALTER TABLE workflow_executions ALTER COLUMN created_at SET NOT NULL;

-- Na v0.1 o campo started_at era preenchido na criação.
-- Para registros históricos RUNNING/COMPLETED a hora exata do início não é recuperável.
UPDATE workflow_executions SET started_at = NULL WHERE status = 'PENDING';
ALTER TABLE workflow_executions ALTER COLUMN started_at DROP NOT NULL;

CREATE INDEX idx_execution_workflow_created_at
    ON workflow_executions (workflow_id, created_at DESC);

ALTER TABLE workflows
    ADD CONSTRAINT chk_workflow_status CHECK (status = 'ACTIVE');

ALTER TABLE workflow_executions
    ADD CONSTRAINT chk_execution_status
    CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED'));

ALTER TABLE workflow_executions
    ADD CONSTRAINT chk_execution_lifecycle
    CHECK (
        (status = 'PENDING' AND started_at IS NULL AND finished_at IS NULL)
        OR (status = 'RUNNING' AND started_at IS NOT NULL AND finished_at IS NULL)
        OR (status = 'COMPLETED' AND started_at IS NOT NULL AND finished_at IS NOT NULL)
    );

