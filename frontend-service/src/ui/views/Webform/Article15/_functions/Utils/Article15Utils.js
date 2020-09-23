import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

const getIndexFromName = (data = [], name) => {
  const table = data.filter(table => table.tableSchemaName === name);

  if (!isEmpty(table)) return table[0].index;
};

const getNameFromIndex = (data = [], index) => {
  const table = data.filter(table => table.index === index);

  if (!isEmpty(table)) return table[0].tableSchemaName;
};

const getUrlParamValue = param => {
  let value = '';
  let queryString = window.location.search;
  const params = queryString.substring(1, queryString.length).split('&');
  params.forEach(parameter => {
    if (parameter.includes(param)) value = parameter.split('=')[1];
  });
  return param === 'tab' ? Number(value) : value === 'true';
};

const getWebformTabs = (allTables = []) => {
  const initialValues = {};

  const tables = allTables.map(table => table.tableSchemaName);

  const value = getNameFromIndex(allTables, getUrlParamValue('tab'));

  compact(tables).forEach(table => {
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

const parseNewRecordData = (columnsSchema, data) => {
  if (!isEmpty(columnsSchema)) {
    let fields;

    if (!isUndefined(columnsSchema)) {
      fields = columnsSchema.map(column => {
        return {
          fieldData: { [column.fieldSchemaId]: null, type: column.type, fieldSchemaId: column.fieldSchemaId }
        };
      });
    }

    const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

    obj.datasetPartitionId = null;
    //dataSetPartitionId is needed for checking the rows owned by delegated contributors
    if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

    return obj;
  }
};

export const Article15Utils = { getIndexFromName, getWebformTabs, mergeArrays, parseNewRecordData };
