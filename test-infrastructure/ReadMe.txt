Para lanzar los test hay que realizar los siguientes pasos
declarar variables de entorno.

	URL_BASE= la url del sistema objetivo (no el endpoint) y que no acabe en /. Ejemplo: https://rn3staging-api.eionet.europa.eu
	LOAD_TEST_PATH= El path completo hasta donde encontrar el fichero con las pruebas a ejecutar. 
	Ejemplo: C:\\proyectos\\EEA\\desarrollo\\repornet\\test-infrastructure\\src\\test\\scala\\resources\\load_test.yml

	Si se usa feeder los ficheros de datos deben estar en la raiz del proyecto
	y llamarse como el nombre de caso de prueba seguido de _param.csv. 
	Ejemplo: cloneDataflowWithData_param.csv
  
  	TESTS:
		File import/export
		import/export from FME (sirven los de import y export solo hay que configurar una external integration)
		Cloning schema
			http://rn3sandbox-api.altia.es/dataschema/copy?sourceDataflow=11&targetDataflow=12
		Data Collection creation (and dataset)
		Release to DC
		Validation
			/validation/dataset/5
		Create snapshot	
			http://rn3sandbox-api.altia.es/snapshot/dataschema/607e8b5d1e32710001628460/dataset/5/create?description=ds1
		Restore snapshot
			/snapshot/6/dataschema/5/restore
  
Ejecución:
	mvn gatling:test