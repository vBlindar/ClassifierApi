
create table if not exists characteristics
(
    id   bigserial
        primary key,
    name varchar(5000)
);

create table characteristic_value
(
    id                bigserial
        primary key,
    value             varchar(5000),
    characteristic_id bigint references characteristics (id) on delete cascade on update cascade
);

create table classifiable_texts
(
    id   bigserial
        primary key,
    text text
);

create table classifiable_texts_characteristics
(
    characteristic_pr_id       bigint not null
            references characteristics(id) on delete cascade on update cascade ,
    classifiable_text_pr_id    bigint not null
            references classifiable_texts(id) on delete cascade on update cascade ,
    characteristic_value_pr_id bigint not null
            references characteristic_value(id) on delete cascade on update cascade ,
    primary key (characteristic_pr_id, characteristic_value_pr_id, classifiable_text_pr_id)
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


