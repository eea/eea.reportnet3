-- DROP THE COLUMN RELEASING FROM THE TABLE REPRESENTATIVE
DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='representative' and column_name='releasing')
  THEN
      ALTER TABLE "public"."representative" DROP COLUMN "releasing";
  END IF;
END $$;
COMMIT;
-- ADD THE COLUMN RELEASING INTO THE TABLE DATASET
ALTER TABLE public."dataset" ADD COLUMN IF NOT EXISTS releasing bool NULL DEFAULT false;
