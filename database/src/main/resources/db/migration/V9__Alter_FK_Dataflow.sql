
-- DROP AND ADD AGAIN THE DIFFERENT FOREIGN KEYS OF THE RELATIONS TO DATAFLOW. TO ADD DELETE CASCADE WHEN A DATAFLOW IS DELETED

ALTER TABLE public."document" DROP CONSTRAINT IF EXISTS document_dataflow_fkey;
ALTER TABLE public."document" ADD CONSTRAINT document_dataflow_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE;

ALTER TABLE public.contributor DROP CONSTRAINT IF EXISTS dataflow_contributor_fkey;
ALTER TABLE public.contributor ADD CONSTRAINT dataflow_contributor_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE;

ALTER TABLE public.weblink DROP CONSTRAINT IF EXISTS weblink_dataflow_fkey;
ALTER TABLE public.weblink ADD CONSTRAINT weblink_dataflow_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE;

ALTER TABLE public.representative DROP CONSTRAINT IF EXISTS  dataflow_id;
ALTER TABLE public.representative DROP CONSTRAINT IF EXISTS  dataflow_fk;
ALTER TABLE public.representative ADD CONSTRAINT dataflow_fk FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE;

ALTER TABLE public.dataflow_user_request DROP CONSTRAINT IF EXISTS user_request_dataflow_pkey;
ALTER TABLE public.dataflow_user_request ADD CONSTRAINT user_request_dataflow_pkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE;

commit;
