-- Create sequence for task ID generation
CREATE SEQUENCE task_id_seq START 1 INCREMENT 1;

-- Create task table
CREATE TABLE tasks
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('task_id_seq'),
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    due_date    DATE,
    priority    VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL
);
