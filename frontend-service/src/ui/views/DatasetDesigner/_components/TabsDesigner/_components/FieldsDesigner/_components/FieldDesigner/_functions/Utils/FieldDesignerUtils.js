import isNil from 'lodash/isNil';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  console.log('fieldPkId', { fieldPkId });
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table =>
      table.records.forEach(record =>
        record.fields.forEach(field => {
          if (!isNil(field) && !isNil(field.referencedField)) {
            console.log('field', field.referencedField.idPk);
          }
          if (!isNil(field) && !isNil(field.referencedField) && field.referencedField.idPk === fieldPkId) {
            referencedFields++;
          }
        })
      )
    )
  );
  console.log('referencedFields', referencedFields);
  return referencedFields;
};

export const FieldDesignerUtils = {
  getCountPKUseInAllSchemas
};
