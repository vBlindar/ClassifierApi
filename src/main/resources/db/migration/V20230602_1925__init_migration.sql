
create table texts_characteristics
(
    id                bigserial
        primary key,
    value             varchar(5000)
);

create table classifiable_texts
(
    id   bigserial
        primary key,
    text text,
    characteristic_id bigint references texts_characteristics (id) on delete cascade on update cascade
);


create table vocabulary
(
    id    bigserial
        primary key,
    value varchar(5000)
);

create table images_characteristics(
    id bigserial primary key,
    value varchar(50)


);


