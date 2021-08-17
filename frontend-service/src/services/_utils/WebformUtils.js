import isEmpty from 'lodash/isEmpty';

import { TextUtils } from 'repositories/_utils/TextUtils';

const parsePamTables = (tables, pamId, type) => {
  return tables.map(table => ({
    idTableSchema: table.tableSchemaId,
    records: [
      {
        fields: parsePamFields(!isEmpty(table.records) ? table.records[0].fields : [], pamId, type),
        id: null,
        idRecordSchema: !isEmpty(table.records) ? table.records[0].recordSchemaId : null
      }
    ]
  }));
};

const parsePamFields = (fields, pamId, type) => {
  return fields.map(field => ({
    id: null,
    idFieldSchema: field.fieldId || field.fieldSchema,
    type: field.type,
    value: getPamFieldValue(field.name, pamId, type)
  }));
};

const getPamFieldValue = (fieldName, pamId, type) => {
  if (TextUtils.areEquals(fieldName, 'id')) return pamId;
  if (TextUtils.areEquals(fieldName, 'fk_pams')) return pamId;
  if (TextUtils.areEquals(fieldName, 'isGroup')) return type;
  if (TextUtils.areEquals(fieldName, 'Id_SectorObjectives')) return `${pamId}_1`;

  return null;
};

export const WebformUtils = {
  parsePamTables
};
