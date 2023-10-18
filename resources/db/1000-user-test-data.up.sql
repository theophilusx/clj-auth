insert into
auth.users
(email, first_name, last_name, created_by, modified_by)
values
('john@example.com', 'John', 'Smith', 'system', 'system'),
('jane@example.com', 'Jane', 'Brown', 'system', 'system'),
('bobby@exmaple.com', 'Bobby', 'Black', 'system', 'system');

with user_sel as ( select user_id, 'user', 'system' from users )
    insert into auth.roles (user_id, user_role, created_by) select * from user_sel;
