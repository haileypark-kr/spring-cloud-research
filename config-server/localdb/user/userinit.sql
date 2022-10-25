create table tbl_users
(

    id         int auto_increment primary key,

    user_id    varchar(50),

    email      varchar(50),

    pwd        varchar(20),

    name       varchar(20),

    created_at datetime default NOW()

);
