CREATE DATABASE IF NOT EXISTS fx_alert_db;
USE fx_alert_db;

CREATE TABLE currency_pairs (
                                id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                base       VARCHAR(3)  NOT NULL,
                                quote      VARCHAR(3)  NOT NULL,
                                created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE KEY uq_base_quote (base, quote)
);

CREATE TABLE rate_history (
                              id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                              pair_id    BIGINT         NOT NULL,
                              rate       DECIMAL(18,6)  NOT NULL,
                              fetched_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_rate_pair FOREIGN KEY (pair_id) REFERENCES currency_pairs(id)
);

CREATE TABLE alert_subscriptions (
                                     id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     pair_id          BIGINT         NOT NULL,
                                     user_email       VARCHAR(255)   NOT NULL,
                                     threshold_type   ENUM('ABOVE','BELOW') NOT NULL,
                                     threshold_value  DECIMAL(18,6)  NOT NULL,
                                     webhook_url      VARCHAR(500),
                                     active           BOOLEAN        NOT NULL DEFAULT TRUE,
                                     CONSTRAINT fk_alert_pair FOREIGN KEY (pair_id) REFERENCES currency_pairs(id)
);

CREATE TABLE delivery_log (
                              id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                              subscription_id     BIGINT NOT NULL,
                              rate                DECIMAL(18,6) NOT NULL,
                              status              ENUM('SUCCESS', 'FAILED') NOT NULL,
                              error_message       VARCHAR(500),
                              delivered_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_delivery_subscription
                                  FOREIGN KEY (subscription_id) REFERENCES alert_subscriptions(id)
);