-- Table: ACCOUNT_CURRENCIES

-- DROP TABLE IF EXISTS "account_currencies";

CREATE TABLE IF NOT EXISTS public."account_currencies"
(
    account_id integer NOT NULL,
    currency_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
    balance_amount double precision NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    CONSTRAINT "pk_ac_acc_cuurr" PRIMARY KEY ("account_id", "currency_id"),
    CONSTRAINT "fk_ac_acc_id" FOREIGN KEY ("account_id")
        REFERENCES "account" ("account_id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT "fk_ac_curr_id" FOREIGN KEY ("currency_id")
        REFERENCES "currencies" ("currency_id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)