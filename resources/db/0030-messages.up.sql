create table if not exists auth.messages (
  msg_name varchar(10) primary key
  , message text not null
);

insert into auth.messages (
  msg_name, message
) values 
  ('verify',
  'A new account has been created. However, before this account can be used,
  it must be confirmed. A confirmation link has been sent to the email address
  when registering the account. Please check your email and follow the link
  provided in order to complete the registration of this account.'),
  ('recover',
  'An email has been sent to the address associated with this account
  containing details on how to reset the passowrd for this account.
  Please follow the instructions in that email to reset your password.'),
  ('locked',
  'Due to suspicious activity associated with this account, the account
  has been temporarily locked. Please wait at least 30 minutes before
  trying again.'),
  ('contact',
  'This account has been suspended. To find out why or to re-activate this
  account, please contact support.');

