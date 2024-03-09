create table if not exists auth.requests (
  req_id int generated always as identity
  , req_key varchar(40) not null
  , user_id int references auth.users(user_id)
  , req_type varchar(20) not null
  , completed boolean default false
  , created_by varchar(100) default 'system'
  , created_dt timestamp default current_timestamp
  , completed_dt timestamp
  , completed_by varchar(100)
  , remote_addr varchar(32)
  , constraint request_pkey primary key (req_key, user_id)
);
