-- Table: account_transaction

-- DROP TABLE IF EXISTS "account_transaction";

CREATE SEQUENCE transaction_id_seq;

CREATE TABLE IF NOT EXISTS public."account_transaction"
(
    transaction_id integer NOT NULL DEFAULT nextval('transaction_id_seq'),
    account_id integer NOT NULL,
    currency_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
	tran_direction character varying  NOT NULL,
    tran_amount double precision NOT NULL,
	tran_description character varying  NOT NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    CONSTRAINT "pk_ac_tran_id" PRIMARY KEY ("transaction_id"),
    CONSTRAINT "fk_ac_tran_dir" FOREIGN KEY ("tran_direction")
        REFERENCES "transaction_direction" ("direction_id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT "fk_ac_tran_curr_id" FOREIGN KEY ("currency_id")
        REFERENCES "currencies" ("currency_id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)