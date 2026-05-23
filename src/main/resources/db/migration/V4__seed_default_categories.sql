insert into categories (code, name, type)
values
    ('FOOD', 'Еда', 'EXPENSE'),
    ('CAFE', 'Рестораны', 'EXPENSE'),
    ('COFFEE', 'Кофе', 'EXPENSE'),
    ('TAXI', 'Такси', 'EXPENSE'),
    ('HOME', 'Дом', 'EXPENSE'),
    ('CHILD', 'Ребенок', 'EXPENSE'),
    ('HEALTH', 'Здоровье', 'EXPENSE'),
    ('FUN', 'Развлечения', 'EXPENSE'),
    ('SALARY', 'Зарплата', 'INCOME'),
    ('GIFT', 'Подарок', 'INCOME'),
    ('REFUND', 'Возврат', 'INCOME')
on conflict (code) do nothing;