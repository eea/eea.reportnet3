ALTER TABLE public."snapshot" ADD COLUMN IF NOT EXISTS "eu_released" bool NULL;
DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='snapshot' and column_name='release')
  THEN
      ALTER TABLE "public"."snapshot" RENAME COLUMN "release" TO "dc_released";
  END IF;
END $$;

COMMIT;