# --- !Ups

create table "board" (
  "name" varchar not null,
  "ip" varchar not null,
  "role" varchar not null,
  "port" int not null
);


# --- !Downs

drop table "board";
