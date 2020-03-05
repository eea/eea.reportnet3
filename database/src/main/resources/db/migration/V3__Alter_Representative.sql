
--- New boolean fields to check the state of the button to download the receipt of the release snapshot
ALTER TABLE public.representative ADD receipt_downloaded bool NOT NULL DEFAULT false;
ALTER TABLE public.representative ADD receipt_outdated bool NOT NULL DEFAULT false;

commit;