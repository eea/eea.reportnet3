import isEmpty from 'lodash/isEmpty';
import lowerFirst from 'lodash/lowerFirst';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getFieldSchemaId = (data = [], selectedTableSchemaId, rootPkFieldId, rootTableName) => {
  if (!isEmpty(data)) {
    const table = data.filter(table => table.tableSchemaId === selectedTableSchemaId);
    if (!isEmpty(table)) {
      const { fieldSchema, fieldId } = table[0].records[0].fields.filter(field => {
        if (TextUtils.areEquals(table[0].name, rootTableName)) return TextUtils.areEquals(field.fieldId, rootPkFieldId);

        return TextUtils.areEquals(field?.referencedField?.idPk, rootPkFieldId);
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
      element =>
        (data = {
          ...data,
          [element.pk === true ? 'id' : lowerFirst(element.name)]: element.value,
          recordId: record.recordId
        })
    );
    return data;
  });

  return typeList.sort((a, b) => a.id - b.id);
};

const checkErrors = (data, rootPkFieldId) => {
  if (isEmpty(data)) return {};

  let errors = {};

  const rootTable = data.filter(table => table.isRootTable === true);
  const restTables = data.filter(table => !(table.isRootTable === true));

  rootTable.forEach(table => {
    const requiredFields = table.elements.filter(element => element.required);

    const fields = requiredFields.map(
      requiredField =>
        (requiredField = { name: requiredField.name, isMissing: !requiredField.hasOwnProperty('fieldId') })
    );

    errors = {
      ...errors,
      [table.name]: { fields, table: { name: table.name, isMissing: !table.hasOwnProperty('tableSchemaId') } }
    };
  });

  restTables.forEach(table => {
    const fk = table.records
      ? table.records[0].fields.filter(field => field?.referencedField?.idPk === rootPkFieldId)
      : [];

    errors = {
      ...errors,
      [table.name]: {
        fields: [{ name: fk[0].name, isMissing: !isEmpty(fk) ? !fk[0].hasOwnProperty('fieldId') : true }],
        table: { name: table.name, isMissing: !table.hasOwnProperty('tableSchemaId') }
      }
    };
  });

  return errors || {};
};

const hasErrors = (data, rootPkFieldId) => {
  const errors = [];

  const rootTable = data.filter(table => table.isRootTable === true);
  const restTables = data.filter(table => !(table.isRootTable === true));

  rootTable.forEach(table => {
    const requiredFields = table.elements.filter(element => element.required);

    requiredFields.forEach(field => {
      errors.push(!field.hasOwnProperty('fieldId'), !table.hasOwnProperty('tableSchemaId'));
    });
  });

  restTables.forEach(table => {
    const fk = table.records
      ? table.records[0].fields.filter(field => field?.referencedField?.idPk === rootPkFieldId)
      : [];
    errors.push(!isEmpty(fk) ? !fk[0].hasOwnProperty('fieldId') : true, !table.hasOwnProperty('tableSchemaId'));
  });

  return errors.includes(true);
};

export const EntitiesWebformUtils = {
  checkErrors,
  getFieldSchemaId,
  getTypeList,
  hasErrors
};
