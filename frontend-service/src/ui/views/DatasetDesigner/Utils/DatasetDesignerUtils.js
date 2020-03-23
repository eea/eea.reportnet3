import isNil from 'lodash/isNil';

const getIndexById = (datasetSchemaId, datasetSchemasArray) => {
  return datasetSchemasArray
    .map(datasetSchema => {
      return datasetSchema.datasetSchemaId;
    })
    .indexOf(datasetSchemaId);
};

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table => {
      if (!table.addTab) {
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

export const DatasetDesignerUtils = {
  getIndexById,
  getCountPKUseInAllSchemas
};
