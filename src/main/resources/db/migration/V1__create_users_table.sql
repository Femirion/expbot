create table if not exists users (
    id bigserial primary key,
    telegram_user_id bigint not null,
    telegram_username varchar(255),
    display_name varchar(255) not null,
    status varchar(32) not null default 'ACTIVE',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,

    constraint uk_users_telegram_user_id unique (telegram_user_id)
);

create index if not exists idx_users_display_name on users(display_name);
create index if not exists idx_users_status on users(status);