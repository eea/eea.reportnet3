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
(32,'Year_BW','Integer',15)
,(34,'BWType','Integer',15)
,(44,'Class','Integer',39)
,(52,'ConcIE','Integer',48)
,(53,'ConcEC','Integer',48)
,(28,'WBName','String',15)
,(16,'BWID','String',15)
,(17,'BWName','String',15)
,(31,'BWKey','String',15)
,(19,'Longitude_BW','CoordinateLong',15)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(20,'Latitude_BW','CoordinateLat',15)
,(33,'AccessKey','String',15)
,(21,'Coordsys_BW','String',15)
,(22,'GroupID','String',15)
,(23,'RBDID','String',15)
,(24,'RBDName','String',15)
,(25,'RBDSUID','String',15)
,(26,'RBDSUName','String',15)
,(27,'WBID','String',15)
,(35,'Change','String',15)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(36,'Closed','String',15)
,(37,'BWaterCat','String',15)
,(38,'SpecGeoCon','String',15)
,(18,'ShortName','String',15)
,(29,'NWUnitID','String',15)
,(30,'NWUnitName','String',15)
,(40,'BWID','String',39)
,(41,'GroupID','String',39)
,(42,'StartDate','Date',39)
,(43,'EndDate','Date',39)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(45,'ManMeas','String',39)
,(46,'Changes','String',39)
,(47,'NuSeasons','Integer',39)
,(49,'BWID','String',48)
,(50,'GroupID','String',48)
,(51,'SampleDate','Date',48)
,(54,'Rem','String',48)
,(56,'BWID','String',55)
,(57,'GroupID','String',55)
,(58,'StartDateA','Date',55)
;
INSERT INTO public.table_headers_collection (id,header_name,header_type,id_table) VALUES 
(59,'EndDateA','Date',55)
,(61,'BWID','String',60)
,(62,'GroupID','String',60)
,(63,'StartDateS','Date',60)
,(64,'EndDateS','Date',60)
;

commit;


