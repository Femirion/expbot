create table if not exists money_transactions (
    id bigserial primary key,
    telegram_message_id bigint not null,
    telegram_user_id bigint not null,
    chat_id bigint not null,
    category_id bigint not null references categories(id),
    type varchar(32) not null,
    amount numeric(14, 2) not null,
    note varchar(255),
    occurred_at timestamp with time zone not null,
    created_at timestamp with time zone not null default current_timestamp,

    constraint uk_money_transactions_message_chat unique (telegram_message_id, chat_id)
);

create index if not exists idx_money_transactions_category_id on money_transactions(category_id);
create index if not exists idx_money_transactions_type on money_transactions(type);
create index if not exists idx_money_transactions_occurred_at on money_transactions(occurred_at);
create index if not exists idx_money_transactions_telegram_user_id on money_transactions(telegram_user_id);
