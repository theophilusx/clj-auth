create type auth.status as enum (
  'unconfirmed'
  , 'confirmed'
  , 'locked'
  , 'contact'
  , 'archived'
);

