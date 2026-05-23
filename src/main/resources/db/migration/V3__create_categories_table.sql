create table if not exists categories (
    id bigserial primary key,
    code varchar(64) not null,
    name varchar(128) not null,
    type varchar(32) not null,
    is_active boolean not null default true,
    created_at timestamp not null default current_timestamp,

    constraint uk_categories_code unique (code)
);

create index if not exists idx_categories_type on categories(type);
create index if not exists idx_categories_is_active on categories(is_active);