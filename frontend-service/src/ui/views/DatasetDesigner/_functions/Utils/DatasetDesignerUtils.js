import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table => {
      if (!isUndefined(table.records) && !table.addTab) {
        table.records.forEach(record =>
          record.fields.forEach(field => {
            if (!isNil(field) && !isNil(field.referencedField) && !isNil(field.referencedField.name)) {
              if (
                !isNil(field) &&
                !isNil(field.referencedField) &&
                field.referencedField.referencedField.fieldSchemaId === fieldPkId
              ) {
                referencedFields++;
              }
            } else {
              if (!isNil(field) && !isNil(field.referencedField) && field.referencedField.idPk === fieldPkId) {
                referencedFields++;
              }
            }
          })
        );
      }
    })
  );
  return referencedFields;
};

const getIndexById = (datasetSchemaId, datasetSchemasArray) => {
  return datasetSchemasArray.map(datasetSchema => datasetSchema.datasetSchemaId).indexOf(datasetSchemaId);
};

const getUrlParamValue = param => {
  let value = '';
  let queryString = window.location.search;
  const params = queryString.substring(1, queryString.length).split('&');
  params.forEach(parameter => {
    if (parameter.includes(param)) {
      value = parameter.split('=')[1];
    }
  });
  return param === 'tab' ? Number(value) : value === 'true';
};

export const DatasetDesignerUtils = {
  getCountPKUseInAllSchemas,
  getIndexById,
  getUrlParamValue
};
