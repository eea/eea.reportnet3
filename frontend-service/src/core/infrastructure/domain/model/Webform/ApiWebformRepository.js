import capitalize from 'lodash/capitalize';
import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { apiWebform } from 'core/infrastructure/api/domain/model/Webform';

import { CoreUtils } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

const addPamsRecords = async (datasetId, tables, pamId, type) => {
  console.log('test', parsePamTables(tables, pamId, type));
  return await apiWebform.addPamsRecords(datasetId, parsePamTables(tables, pamId, type));
};

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

  return null;
};

export const ApiWebformRepository = { addPamsRecords };
