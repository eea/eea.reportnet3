DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='snapshot' and column_name='blocked')
  THEN
      ALTER TABLE "public"."snapshot" DROP COLUMN "blocked";
  END IF;
END $$;
COMMIT;