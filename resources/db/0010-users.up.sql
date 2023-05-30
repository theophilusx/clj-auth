create table if not exists auth.users (
  email varchar(100) primary key
  , first_name varchar(100)
  , last_name varchar(100)
  , password varchar(1024) default 'unset'
  , id_status status default 'verify'
  , created_by varchar(100) not null
  , created_dt timestamp default current_timestamp
  , modified_by varchar(100) not null
  , modified_dt timestamp default current_timestamp
);

