-- Create sequence
CREATE SEQUENCE IF NOT EXISTS public.preparation_dataset_id_seq
    INCREMENT BY 1
    MINVALUE 1
    START 1
    CACHE 1
    NO CYCLE;

-- Create table
CREATE TABLE IF NOT EXISTS public.preparation_dataset (
    id BIGINT NOT NULL DEFAULT nextval('preparation_dataset_id_seq'),

    dataset_name VARCHAR(255) NOT NULL,

    is_created BOOLEAN NOT NULL DEFAULT false,

    code VARCHAR(255) NOT NULL,

    dataflow_id BIGINT NOT NULL,

    data_provider_id BIGINT NOT NULL,

    CONSTRAINT preparation_dataset_pkey
    PRIMARY KEY (id)
    );

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_prep_ds_dataflow
    ON public.preparation_dataset (dataflow_id);

CREATE INDEX IF NOT EXISTS idx_prep_ds_provider
    ON public.preparation_dataset (data_provider_id);

-- Grant permissions
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE
    ON TABLE public.preparation_dataset
    TO dataflow, dataset, recordstore, testuser, validation;

GRANT USAGE, SELECT, UPDATE
    ON SEQUENCE public.preparation_dataset_id_seq
    TO dataflow, dataset, recordstore, testuser, validation;