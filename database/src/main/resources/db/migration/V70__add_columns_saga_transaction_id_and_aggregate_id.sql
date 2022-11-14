alter table public.process add column if not exists "saga_transaction_id" text null;
alter table public.process add column if not exists "aggregate_id" text null;