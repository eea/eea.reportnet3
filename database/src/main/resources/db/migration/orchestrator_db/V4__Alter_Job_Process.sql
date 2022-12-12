--Alter table job_process add columns saga_transaction_id and aggregate_id --
alter table public.job_process add column if not exists "dataset_id" int8 null;
alter table public.job_process add column if not exists "saga_transaction_id" text null;
alter table public.job_process add column if not exists "aggregate_id" text null;
