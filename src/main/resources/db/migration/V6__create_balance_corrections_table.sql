create table if not exists balance_corrections (
    id bigserial primary key,
    telegram_message_id bigint not null,
    telegram_user_id bigint not null,
    chat_id bigint not null,
    amount numeric(14, 2) not null,
    target_balance numeric(14, 2) not null,
    previous_balance numeric(14, 2) not null,
    occurred_at timestamp with time zone not null,
    created_at timestamp with time zone not null default now(),
    constraint uk_balance_corrections_message_chat unique (telegram_message_id, chat_id)
);

create index if not exists idx_balance_corrections_chat_id on balance_corrections(chat_id);
