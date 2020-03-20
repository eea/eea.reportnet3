import isNil from 'lodash/isNil';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table =>
      table.records.forEach(record =>
        record.fields.forEach(field => {
          if (!isNil(field) && !isNil(field.referencedField) && field.referencedField.idPk === fieldPkId) {
            referencedFields++;
          }
        })
      )
    )
  );
  return referencedFields;
};

export const FieldDesignerUtils = {
  getCountPKUseInAllSchemas
};
