-- Table: transaction_direction

-- DROP TABLE IF EXISTS "transaction_direction";

CREATE TABLE IF NOT EXISTS "transaction_direction"
(
    direction_id character varying(3)  NOT NULL,
    direction_desc character varying(100),
    CONSTRAINT direction_id_pkey PRIMARY KEY ("direction_id")
)