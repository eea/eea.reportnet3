import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table => {
      if (!isUndefined(table.records) && !table.addTab) {
        table.records.forEach(record =>
          record.fields.forEach(field => {
            if (
              !isNil(field) &&
              field.type.toUpperCase() === 'LINK' &&
              !isNil(field.referencedField) &&
              !isNil(field.referencedField.name)
            ) {
              if (
                !isNil(field) &&
                !isNil(field.referencedField) &&
                field.referencedField.referencedField.fieldSchemaId === fieldPkId
              ) {
                referencedFields++;
              }
            } else {
              if (
                !isNil(field) &&
                field.type.toUpperCase() === 'LINK' &&
                !isNil(field.referencedField) &&
                field.referencedField.idPk === fieldPkId
              ) {
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

export const DatasetDesignerUtils = {
  getCountPKUseInAllSchemas,
  getIndexById
};
