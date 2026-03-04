CREATE INDEX IF NOT EXISTS dataset_dataflowid_idx ON public.dataset (dataflowid);

CREATE INDEX IF NOT EXISTS snapshot_reporting_dataset_id_idx ON public.snapshot (reporting_dataset_id);

CREATE INDEX IF NOT EXISTS process_dataflow_dataset_status_idx ON public.process (dataflow_id, dataset_id, status);