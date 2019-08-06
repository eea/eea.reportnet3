INSERT INTO public.dataflow (description, "name", creation_date, deadline_date, status) VALUES('dataflow_1', 'dataflow_1', current_timestamp, '01-01-2020', 'ACCEPTED');
INSERT INTO public.user_request (id, user_requester, user_requested, request_type) VALUES(1, 2, 2, 'ACCEPTED');
INSERT INTO public.dataflow_user_request (dataflow_id, user_request_id) VALUES(1,1);
INSERT INTO public.dataset (id, date_creation, dataset_name, dataflowid, status, url_connection, visibility) VALUES(1, current_timestamp, 'Dataset_1', 1, 'OK', '', 'ALL');
INSERT INTO public.reporting_dataset (id) VALUES(1);
INSERT INTO public.contributor (id, email, user_id, dataflow_id) VALUES(1, 'test@eea.eu', 1, 1);
INSERT INTO public.partition_dataset (id, user_name, id_dataset) VALUES(1,'root',1);
