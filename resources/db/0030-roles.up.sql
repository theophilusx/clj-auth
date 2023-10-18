create table if not exists auth.roles (
  user_id int references users(user_id)
  , user_role varchar(50) references role_definition(role_name)
  , created_dt timestamp default current_timestamp
  , created_by varchar(100) not null
  , CONSTRAINT roles_pk primary key (user_id, user_role)
);
