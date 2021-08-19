import isEmpty from 'lodash/isEmpty';
import lowerFirst from 'lodash/lowerFirst';

import { TextUtils } from 'repositories/_utils/TextUtils';

const requiredFields = ['Id', 'IsGroup', 'ShortDescription', 'Title'];

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

const checkErrors = data => {
  if (isEmpty(data)) return {};

  let errors = {};

  const pamsTable = data.filter(table => TextUtils.areEquals(table.name, 'PaMs'));
  const restTables = data.filter(table => !TextUtils.areEquals(table.name, 'PaMs'));

  pamsTable.forEach(table => {
    const fields = requiredFields.map(field => {
      const filteredField = table.elements.filter(element => TextUtils.areEquals(element.name, field))[0];

      return { name: field, isMissing: !filteredField.hasOwnProperty('fieldId') };
    });

    errors = {
      ...errors,
      [table.name]: { fields, table: { name: table.name, isMissing: !table.hasOwnProperty('tableSchemaId') } }
    };
  });

  restTables.forEach(table => {
    const fk = table.records ? table.records[0].fields.filter(field => TextUtils.areEquals(field.name, 'Fk_PaMs')) : [];

    errors = {
      ...errors,
      [table.name]: {
        fields: [{ name: 'Fk_PaMs', isMissing: !isEmpty(fk) ? !fk[0].hasOwnProperty('fieldId') : true }],
        table: { name: table.name, isMissing: !table.hasOwnProperty('tableSchemaId') }
      }
    };
  });

  return errors || {};
};

const getSingleRecordOption = singleRecord => {
  if (singleRecord.elements.find(el => TextUtils.areEquals(el.name, 'Id')).value === '') {
    return `${singleRecord.elements.find(el => TextUtils.areEquals(el.name, 'Id')).value}`;
  }

  return `${singleRecord.elements.find(el => TextUtils.areEquals(el.name, 'Id')).value}`;
};

const hasErrors = data => {
  const errors = [];

  const pamsTable = data.filter(table => TextUtils.areEquals(table.name, 'PaMs'));
  const restTables = data.filter(table => !TextUtils.areEquals(table.name, 'PaMs'));

  pamsTable.forEach(table => {
    requiredFields.forEach(field => {
      const filteredField = table.elements.filter(element => TextUtils.areEquals(element.name, field))[0];

      errors.push(!filteredField.hasOwnProperty('fieldId'), !table.hasOwnProperty('tableSchemaId'));
    });
  });

  restTables.forEach(table => {
    const fk = table.records ? table.records[0].fields.filter(field => TextUtils.areEquals(field.name, 'Fk_PaMs')) : [];
    errors.push(!isEmpty(fk) ? !fk[0].hasOwnProperty('fieldId') : true, !table.hasOwnProperty('tableSchemaId'));
  });

  return errors.includes(true);
};

const parseListOfSinglePams = (records = []) => {
  const options = [];
  records.forEach(record => {
    if (
      record.elements.find(el => TextUtils.areEquals(el.name, 'IsGroup')).value === 'Single' &&
      record.elements.find(el => TextUtils.areEquals(el.name, 'Id')) &&
      record.elements.find(el => TextUtils.areEquals(el.name, 'Title'))
    ) {
      options.push(getSingleRecordOption(record));
    }
  });

  return options.sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }));
};

export const Article13Utils = { checkErrors, getFieldSchemaId, getTypeList, hasErrors, parseListOfSinglePams };
