import isUndefined from 'lodash/isUndefined';

const getUrlParamValue = param => {
  let value = '';
  let queryString = window.location.search;
  const params = queryString.substring(1, queryString.length).split('&');
  params.forEach(parameter => {
    if (parameter.includes(param)) value = parameter.split('=')[1];
  });
  return param === 'tab' ? Number(value) : value === 'true';
};

const getWebformTabs = allTables => {
  const initialValues = {};

  const tables = allTables.map(table => table.index).filter(index => index > -1);

  const value = getUrlParamValue('tab');

  tables.forEach(table => {
    initialValues[table] = false;
    initialValues[value] = true;
  });

  return initialValues;
};

const mergeArrays = (array1 = [], array2 = [], array1Key = '', array2Key = '') => {
  const result = [];

  for (let i = 0; i < array1.length; i++) {
    result.push({ ...array1[i], ...array2.find(element => element[array2Key] === array1[i][array1Key]) });
  }

  return result;
};

const parseRecordData = (columnsSchema = [{ recordId: null }], data) => {
  let fields;

  if (!isUndefined(columnsSchema)) {
    fields = columnsSchema.map(column => ({
      fieldData: { [column.field]: null, type: column.type, fieldSchemaId: column.field }
    }));
  }

  const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

  obj.datasetPartitionId = null;
  //dataSetPartitionId is needed for checking the rows owned by delegated contributors
  if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

  return obj;
};

export const WebformUtils = { getUrlParamValue, getWebformTabs, mergeArrays, parseRecordData };
