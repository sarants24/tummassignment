-- Table: account

-- DROP TABLE IF EXISTS "account";
CREATE SEQUENCE account_id_seq;

CREATE TABLE IF NOT EXISTS "account"
(
    account_id integer NOT NULL DEFAULT nextval('account_id_seq'),
    customer_id character varying(20)  NOT NULL,
    country character varying  NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    CONSTRAINT "account_pkey" PRIMARY KEY ("account_id")
)
