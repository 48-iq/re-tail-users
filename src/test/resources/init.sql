create table users (
    id text primary key not null,
    name text,
    surname text,
    nickname text not null,
    email text,
    phone text,
    address text,
    registered_at timestamp not null,
    about text,
    avatar_image_id text
);

create table user_settings (
    id text primary key not null,
    name_visibility boolean not null default true,
    surname_visibility boolean not null default true,
    email_visibility boolean not null default true,
    phone_visibility boolean not null default true,
    address_visibility boolean not null default true,
    avatar_visibility boolean not null default true,
    user_id text not null unique,
    constraint fk_user
        foreign key (user_id)
        references users (id)
        on delete cascade
);

create table avatars_metadata (
    id text primary key not null,
    avatar_path text not null
);