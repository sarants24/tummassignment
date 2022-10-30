-- Table: account

-- DROP TABLE IF EXISTS "account_message_log";

CREATE TABLE IF NOT EXISTS "account_message_log"
(
    message_id character varying(200) NOT NULL,
    customer_id character varying(20)  NULL,
	account_id integer NULL,
	tran_type character varying(20) NULL,
    message_content character VARYING(1000)  NULL,
	status character varying(20) NULL,
	error_message character varying(2000) NULL,
    created_date timestamp without time zone NOT NULL,
    last_updated_date timestamp without time zone NOT NULL,
    CONSTRAINT "message_id_pkey" PRIMARY KEY ("message_id")
)
