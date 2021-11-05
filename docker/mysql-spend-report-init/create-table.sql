CREATE TABLE spend_report (
    account_id BIGINT       NOT NULL,
    log_ts     TIMESTAMP(3) NOT NULL,
    amount     BIGINT       NOT NULL,
    PRIMARY KEY (account_id, log_ts)
);