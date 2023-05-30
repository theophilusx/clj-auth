create type auth.status as enum (
  'verify'
  , 'recover'
  , 'locked'
  , 'contact'
  , 'ok'
  , 'archived'
);

