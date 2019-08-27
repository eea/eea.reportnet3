-- BATHING WATERS SCHEMA
INSERT INTO public.table_collection (dataflow_id,dataset_id,table_name) VALUES 
(1,1,'Characterisation');



INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'season','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'bathingWaterIdentifier','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'groupIdentifier','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'qualityClass','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'geographicalConstraint','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'link','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Remarks','TEXT', max(id) from table_collection);


INSERT INTO public.table_collection (dataflow_id,dataset_id,table_name) VALUES 
(1,1,'SeasonalPeriod');



INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'season','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'bathingWaterIdentifier','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'periodType','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'startDate','DATE', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'endDate','DATE', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'managementMeasures','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Remarks','TEXT', max(id) from table_collection);

INSERT INTO public.table_collection (dataflow_id,dataset_id,table_name) VALUES 
(1,1,'MonitoringResult');



INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'season','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'bathingWaterIdentifier','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'sampleDate','DATE', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'intestinalEnterococciValue','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'escherichiaColiValue','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'sampleStatus','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'intestinalEnterococciStatus','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'escherichiaColiStatus','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Remarks','TEXT', max(id) from table_collection);

COMMIT;

-- MMR SCHEMA

INSERT INTO public.table_collection (dataflow_id,dataset_id,table_name) VALUES 
(2,2,'MS own proxy dataset (plus)');



INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Country Code','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Country name','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Year','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Gas/Scope','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'CRF code','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Description as in CRF','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Sector_code','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Sector_name','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Colum','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Row','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Unit','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Emissions with notation key [kt]','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Emissions','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Notation key','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Description test','TEXT', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Check','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Manually','NUMBER', max(id) from table_collection);
INSERT INTO public.table_headers_collection (header_name,header_type,id_table) 
(select 'Final check','NUMBER', max(id) from table_collection);

COMMIT;



