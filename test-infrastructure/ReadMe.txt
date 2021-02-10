Para lanzar los test hay que realizar los siguientes pasos
declarar variables de entorno
  URL_BASE= la url del sistema objetivo (no el endpoint) y que no acabe en /. Ejemplo: https://rn3staging-api.eionet.europa.eu
  LOAD_TEST_PATH= El path completo hasta donde encontrar el fichero con las pruebas a ejecutar. Ejemplo: C:\\proyectos\\EEA\\desarrollo\\repornet\\test-infrastructure\\src\\test\\scala\\resources\\load_test.yml

  si se usa feeder los ficheros de datos deben estar en la raiz del proyecto y llamarse como el nombre de caso de prueba seguido de _param.csv. Ejemplo: cloneDataflowWithData_param.csv

Ejecución:
mvn gatling:test

