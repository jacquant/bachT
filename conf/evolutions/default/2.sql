# --- !Ups

create table "card" (
  "title" varchar not null,
  "content" varchar not null
);


# --- !Downs

drop table "card";