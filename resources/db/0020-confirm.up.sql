create table if not exists auth.confirm (
  confirm_id varchar(64) primary key
  , email varchar(100) not null
  , is_confirmed boolean default false
  , created_by varchar(100) not null
  , created_dt timestamp default current_timestamp
  , verified_ip varchar(40)
  , verified_by varchar(100)
  , verified_dt timestamp default current_timestamp
);

