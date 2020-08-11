create table oauth2_authorized_client
(
    client_registration_id varchar(100) not null,
    principal_name varchar(200) not null,
    access_token_type varchar(100) not null,
    access_token_value blob not null,
    access_token_issued_at timestamp default CURRENT_TIMESTAMP not null,
    access_token_expires_at timestamp default CURRENT_TIMESTAMP not null,
    access_token_scopes varchar(1000) null,
    refresh_token_value blob null,
    refresh_token_issued_at timestamp default CURRENT_TIMESTAMP not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    primary key (client_registration_id, principal_name)
);

create table user_oauth2
(
    client_registration_id varchar(100) not null,
    principal_name varchar(200) not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    user_id varchar(255) null,
    primary key (client_registration_id, principal_name)
);

create table users
(
    id int auto_increment
        primary key,
    username varchar(50) not null,
    password varchar(100) not null,
    enabled tinyint(1) null,
    phone varchar(20) null,
    avatar varchar(200) null,
    roles varchar(50) null
);

