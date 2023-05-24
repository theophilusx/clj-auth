create type auth.status as enum (
  'verify'
  , 'recover'
  , 'locked'
  , 'contact'
  , 'ok'
  , 'archived'
);
--;;
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
--;;
create table if not exists auth.confirm (
  confirm_id varchar(64) primary key
  , email varchar(100) not null
  , is_confirmed boolean default false
  , created_by varchar(100) not null
  , created_dt timestamp default current_timestamp
  , verified_ip varchar(40)
  , verified_by varchar(100)
  , verfied_dt timestamp default current_timestamp
);
