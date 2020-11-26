import isEmpty from 'lodash/isEmpty';
import lowerFirst from 'lodash/lowerFirst';

import { TextUtils } from 'ui/views/_functions/Utils';

const getFieldSchemaId = (data = [], selectedTableSchemaId) => {
  if (!isEmpty(data)) {
    const table = data.filter(table => table.tableSchemaId === selectedTableSchemaId);

    if (!isEmpty(table)) {
      const { fieldSchema, fieldId } = table[0].records[0].fields.filter(field => {
        let fieldName = 'Fk_PaMs';

        if (TextUtils.areEquals(table[0].name, 'pams')) fieldName = 'Id';

        return TextUtils.areEquals(field.name, fieldName);
      })[0];

      return { fieldSchema, fieldId };
    }
  }

  return { fieldSchema: null, fieldId: null };
};

const getTypeList = (records = []) => {
  const typeList = records.map(record => {
    let data = {};

    record.elements.forEach(
      element => (data = { ...data, [lowerFirst(element.name)]: element.value, recordId: record.recordId })
    );
    return data;
  });

  return {
    single: typeList.filter(list => list.isGroup === 'Single').sort((a, b) => a.id - b.id),
    group: typeList.filter(list => list.isGroup === 'Group').sort((a, b) => a.id - b.id)
  };
};

export const Article13Utils = { getFieldSchemaId, getTypeList };
