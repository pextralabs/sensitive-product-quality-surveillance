# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table entity (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  type                          varchar(255),
  constraint pk_entity primary key (id)
);

create table sensor (
  id                            bigint auto_increment not null,
  bearer_id                     bigint not null,
  label                         varchar(255),
  value                         double,
  constraint pk_sensor primary key (id)
);

alter table sensor add constraint fk_sensor_bearer_id foreign key (bearer_id) references entity (id) on delete restrict on update restrict;
create index ix_sensor_bearer_id on sensor (bearer_id);


# --- !Downs

alter table sensor drop constraint if exists fk_sensor_bearer_id;
drop index if exists ix_sensor_bearer_id;

drop table if exists entity;

drop table if exists sensor;

