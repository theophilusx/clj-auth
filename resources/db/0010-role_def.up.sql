create table if not exists auth.role_definition (
  role_name varchar(20) primary key
  , role_desc text
  , created_dt timestamp default current_timestamp
  , created_by varchar(100) not null
  , modified_dt timestamp default current_timestamp
  , modified_by varchar(100) not null
);

insert into auth.role_definition
(role_name, role_desc, created_by, modified_by)
values
('user', 'Basic user role', 'system', 'system'),
('admin', 'Basic administrator role', 'system', 'system');
