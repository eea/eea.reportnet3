
-- at dataset database
--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--=
--==--==--==--==--==--==--==--==--==--==--= geometry function new START --==--==--==--==--==--==--==--==--==--==--=
--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--=
--DROP FUNCTION public.insert_geometry_function_notrigger_EEA(int8, int8, int8);

CREATE OR REPLACE FUNCTION public.insert_geometry_function_notrigger_EAA(
    dataset_id bigint,
    limit_val bigint,
    offset_val bigint
)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
DECLARE
geomErr text;
    geom public.geometry;
    idKey text;
    valueKey text;
    query text;
    queryerror text;
    dataset_val TEXT := CONCAT('dataset_', dataset_id::TEXT);
    updated_count bigint := 0;
BEGIN

FOR idKey, valueKey IN
	    EXECUTE FORMAT(
	        'SELECT id::TEXT, value::TEXT
	         FROM %I.field_value fv
	         WHERE fv.type IN (''POINT'',''LINESTRING'',''POLYGON'',''MULTIPOINT'',''MULTILINESTRING'',''MULTIPOLYGON'',''GEOMETRYCOLLECTION'')
	         ORDER BY id
	         OFFSET %s
	         LIMIT %s',
	        dataset_val,
			offset_val,
			limit_val
	    )
		LOOP
begin
--            RAISE NOTICE 'another_func(%,%)', idKey,valueKey;
            geom := public.ST_GeomFromText(public.ST_AsText(public.ST_Transform(public.ST_SetSRID(public.ST_GeomFromGeoJSON(valueKey::json->'geometry'),((valueKey::json->'properties')::json->>'srid')::integer),4326)),4326);
--            RAISE NOTICE 'Geom (%)', geom::text;
            query := 'update dataset_'|| dataset_id ||'.field_value fv set geometry = '''|| geom::text ||''' , geometry_error = null where fv.id = '''||idKey||'''';
--            RAISE NOTICE 'Query: %', query;
execute query;
updated_count := updated_count + 1;
			RAISE NOTICE 'updated count is %', updated_count;
			RAISE NOTICE 'updated id is %', idKey;

exception when others then
                geom := null;
                RAISE NOTICE 'Geom Err';
                if valueKey <> '' then
                    geomErr := sqlstate || ' ' || sqlerrm;
                    RAISE NOTICE 'Geom Err: % in dataset %', geomErr,dataset_id;
                    queryerror := 'update dataset_'|| dataset_id ||'.field_value fv set geometry = null , geometry_error = '''|| geomErr ||''' where fv.id = '''||idKey||'''';
--                    RAISE NOTICE 'Query Err: %', query;
execute queryerror;
else
					RAISE NOTICE 'Skipped update: Empty value for id % in dataset %', idKey, dataset_id;
end if;
end;
END LOOP;

RETURN updated_count;
END;
$function$
;

--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--=
--==--==--==--==--==--==--==--==--==--==--= geometry function new END   --==--==--==--==--==--==--==--==--==--==--=
--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--==--=

