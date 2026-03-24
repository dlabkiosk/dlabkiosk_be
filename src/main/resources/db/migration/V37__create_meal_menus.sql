CREATE TABLE meal_menus (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id    BIGINT      NOT NULL,
    menu_date   DATE        NOT NULL,
    lunch       VARCHAR(500) NULL,
    dinner      VARCHAR(500) NULL,
    is_closed   TINYINT(1)  NOT NULL DEFAULT 0,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    FOREIGN KEY (store_id) REFERENCES stores (id),
    UNIQUE KEY uk_meal_menu_store_date (store_id, menu_date)
);

CREATE INDEX idx_meal_menu_date ON meal_menus (menu_date);
