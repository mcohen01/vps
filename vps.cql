create keyspace if not exists vps 
with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use vps;

create table device (
  device_id text,
  disconnected timestamp,
  primary key (device_id)
);

create table metric (
  device_id text,
  event_time timestamp,
  temperature float,
  current float,
  voltage float,
  primary key (device_id, event_time)
);

create table most_recent_metric (
  device_id text,
  event_time timestamp,
  temperature float,
  current float,
  voltage float,
  primary key (device_id)
);