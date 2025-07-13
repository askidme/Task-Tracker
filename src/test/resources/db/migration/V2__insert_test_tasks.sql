INSERT INTO tasks (id, title, description, due_date, priority, status)
VALUES
    (1001, 'Test Task 1', 'Description 1', current_date, 'HIGH', 'PENDING'),
    (1002, 'Test Task 2', 'Description 2', current_date + INTERVAL '1 day', 'MEDIUM', 'PENDING'),
    (1003, 'Test Task 3', 'Description 3', current_date + INTERVAL '2 days', 'HIGH', 'PENDING');