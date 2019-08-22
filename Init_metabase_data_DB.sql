INSERT INTO public.dataflow (description, "name", creation_date, deadline_date, status) VALUES('dataflow_1', 'dataflow_1', current_timestamp, '01-01-2020', 'ACCEPTED');
INSERT INTO public.user_request (id, user_requester, user_requested, request_type) VALUES(1, 2, 2, 'ACCEPTED');
INSERT INTO public.dataflow_user_request (dataflow_id, user_request_id) VALUES(1,1);
INSERT INTO public.dataset (id, date_creation, dataset_name, dataflowid, status, url_connection, visibility) VALUES(1, current_timestamp, 'Dataset_1', 1, 'OK', '', 'ALL');
INSERT INTO public.reporting_dataset (id) VALUES(1);
INSERT INTO public.contributor (id, email, user_id, dataflow_id) VALUES(1, 'test@eea.eu', 1, 1);
INSERT INTO public.partition_dataset (id, user_name, id_dataset) VALUES(1,'root',1);
--Schema Values
INSERT INTO public.table_collection (id,dataflow_id,dataset_id,table_name) VALUES 
(15,1,1,'BWQD_2006_IdentifiedBW')
,(39,1,1,'BWQD_2006_SeasonalInfo')
,(48,1,1,'BWQD_2006_MonitoringResults')
,(60,1,1,'BWQD_2006_ShortTermPolut')
,(55,1,1,'BWQD_2006_AbnormalSituations')
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(32,'Year_BW','NUMBER',15)
,(34,'BWType','NUMBER',15)
,(44,'Class','NUMBER',39)
,(52,'ConcIE','NUMBER',48)
,(53,'ConcEC','NUMBER',48)
,(28,'WBName','TEXT',15)
,(16,'BWID','TEXT',15)
,(17,'BWName','TEXT',15)
,(31,'BWKey','TEXT',15)
,(19,'Longitude_BW','COORDINATE_LONG',15)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(20,'Latitude_BW','COORDINATE_LAT',15)
,(33,'AccessKey','TEXT',15)
,(21,'Coordsys_BW','TEXT',15)
,(22,'GroupID','TEXT',15)
,(23,'RBDID','TEXT',15)
,(24,'RBDName','TEXT',15)
,(25,'RBDSUID','TEXT',15)
,(26,'RBDSUName','TEXT',15)
,(27,'WBID','TEXT',15)
,(35,'Change','TEXT',15)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(36,'Closed','TEXT',15)
,(37,'BWaterCat','TEXT',15)
,(38,'SpecGeoCon','TEXT',15)
,(18,'ShortName','TEXT',15)
,(29,'NWUnitID','TEXT',15)
,(30,'NWUnitName','TEXT',15)
,(40,'BWID','TEXT',39)
,(41,'GroupID','TEXT',39)
,(42,'StartDate','DATE',39)
,(43,'EndDate','DATE',39)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(45,'ManMeas','TEXT',39)
,(46,'Changes','TEXT',39)
,(47,'NuSeasons','NUMBER',39)
,(49,'BWID','TEXT',48)
,(50,'GroupID','TEXT',48)
,(51,'SampleDate','DATE',48)
,(54,'Rem','TEXT',48)
,(56,'BWID','TEXT',55)
,(57,'GroupID','TEXT',55)
,(58,'StartDateA','DATE',55)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(59,'EndDateA','DATE',55)
,(61,'BWID','TEXT',60)
,(62,'GroupID','TEXT',60)
,(63,'StartDateS','DATE',60)
,(64,'EndDateS','DATE',60)
;

commit;


