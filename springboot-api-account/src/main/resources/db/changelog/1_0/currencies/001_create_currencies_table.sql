-- Table: currencies

-- DROP TABLE IF EXISTS "currencies";

CREATE TABLE IF NOT EXISTS "currencies"
(
    currency_id character varying(3)  NOT NULL,
    currency_desc character varying(100),
    CONSTRAINT currencies_pkey PRIMARY KEY ("currency_id")
)