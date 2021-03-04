update dataset 
set public_file_name  = concat(public_file_name,'.xlsx')
where public_file_name is not null