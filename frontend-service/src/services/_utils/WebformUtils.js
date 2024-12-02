import isEmpty from 'lodash/isEmpty';

import { TextUtils } from 'repositories/_utils/TextUtils';

const parsePamTables = (tables, pamId, type) =>
  tables.map(table => ({
    idTableSchema: table.tableSchemaId,
    records: [
      {
        fields: parsePamFields(!isEmpty(table.records) ? table.records[0].fields : [], pamId, type),
        id: null,
        idRecordSchema: !isEmpty(table.records) ? table.records[0].recordSchemaId : null
      }
    ]
  }));

const parsePamFields = (fields, pamId, type) =>
  fields.map(field => ({
    id: null,
    idFieldSchema: field.fieldId || field.fieldSchema,
    type: field.type,
    value: getPamFieldValue(field.name, pamId, type)
  }));

const getPamFieldValue = (fieldName, pamId, type) => {
  if (TextUtils.areEquals(fieldName, 'id')) {
    return pamId;
  } else if (TextUtils.areEquals(fieldName, 'fk_pams')) {
    return pamId;
  } else if (TextUtils.areEquals(fieldName, 'isGroup')) {
    return type;
  } else if (TextUtils.areEquals(fieldName, 'Id_SectorObjectives')) {
    return `${pamId}_1`;
  } else {
    return null;
  }
};

const parseEntityTables = (tables, entityId, rootPkFieldId) =>
  tables.map(table => ({
    idTableSchema: table.tableSchemaId,
    records: [
      {
        fields: parseEntityFields(!isEmpty(table.records) ? table.records[0].fields : [], entityId, rootPkFieldId),
        id: null,
        idRecordSchema: !isEmpty(table.records) ? table.records[0].recordSchemaId : null
      }
    ]
  }));

const parseEntityFields = (fields, entityId, rootPkFieldId) =>
  fields.map(field => ({
    id: null,
    idFieldSchema: field.fieldId || field.fieldSchema,
    value: getEntityFieldValue(field, entityId, rootPkFieldId)
  }));

const getEntityFieldValue = (field, entityId, rootPkFieldId) => {
  if (TextUtils.areEquals(field.fieldId, rootPkFieldId) || TextUtils.areEquals(field.fieldSchema, rootPkFieldId)) {
    return entityId;
  } else if (TextUtils.areEquals(field?.referencedField?.idPk, rootPkFieldId)) {
    return entityId;
  } else {
    return null;
  }
};

const parseWebformListDTO = webformsDTO => webformsDTO.map(webform => ({ ...webform, name: webform.label }));

export const WebformUtils = {
  parseEntityTables,
  parsePamTables,
  parseWebformListDTO
};
