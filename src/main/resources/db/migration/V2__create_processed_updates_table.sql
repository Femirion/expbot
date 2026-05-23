create table if not exists processed_updates (
    id bigserial primary key,
    telegram_update_id bigint not null,
    processed_at timestamp not null default current_timestamp,

    constraint uk_processed_updates_telegram_update_id unique (telegram_update_id)
);

create index if not exists idx_processed_updates_processed_at on processed_updates(processed_at);