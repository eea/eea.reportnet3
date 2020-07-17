-- CREATE COLUMN IN SNAPSHOT

ALTER TABLE public."snapshot" 
ADD COLUMN IF NOT EXISTS force_release bool;

commit;