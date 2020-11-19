import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const getCountPKUseInAllSchemas = (fieldPkId, datasetSchemas) => {
  let referencedFields = 0;
  datasetSchemas.forEach(schema =>
    schema.tables.forEach(table => {
      if (!isUndefined(table.records) && !table.addTab) {
        table.records.forEach(record =>
          record.fields.forEach(field => {
            if (
              !isNil(field) &&
              TextUtils.areEquals(field.type, 'LINK') &&
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
                TextUtils.areEquals(field.type, 'LINK') &&
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
