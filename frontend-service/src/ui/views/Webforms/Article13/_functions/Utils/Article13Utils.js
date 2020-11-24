import isEmpty from 'lodash/isEmpty';
import lowerFirst from 'lodash/lowerFirst';

import { TextUtils } from 'ui/views/_functions/Utils';

const getTypeList = (records = []) => {
  const typeList = records.map(record => {
    let data = {};
    const pamsSchemaId = record.elements.filter(element => TextUtils.areEquals(element.name, 'id'));

    record.elements.forEach(
      element =>
        (data = {
          ...data,
          [lowerFirst(element.name)]: element.value,
          recordId: record.recordId,
          fieldSchemaPamId: !isEmpty(pamsSchemaId) ? pamsSchemaId[0].fieldSchemaId : element.fieldSchemaId
        })
    );
    return data;
  });

  return {
    single: typeList.filter(list => list.isGroup === 'Single').sort((a, b) => a.id - b.id),
    group: typeList.filter(list => list.isGroup === 'Group').sort((a, b) => a.id - b.id)
  };
};

export const Article13Utils = { getTypeList };
