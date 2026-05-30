create table if not exists category_limits (
    id bigserial primary key,
    category_id bigint not null,
    period varchar(32) not null,
    amount numeric(14, 2) not null,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,

    constraint fk_category_limits_category foreign key (category_id) references categories(id),
    constraint uk_category_limits_category_period unique (category_id, period)
);

create index if not exists idx_category_limits_category_id on category_limits(category_id);
