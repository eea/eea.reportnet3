import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';

import { TextUtils } from 'repositories/_utils/TextUtils';

const parseData = (dataRecords = [], tables, schemaTables) => {
  const records = dataRecords.map(record => {
    const { fields, recordId, recordSchemaId, validations } = record;

    return { fields: parseFields(fields, schemaTables), recordId, recordSchemaId, validations };
  });

  return records.map(rec => ({ ...rec, elements: parseElements(tables.elements, rec.fields) }));
};

const parseElements = (elements = [], fields = []) => {
  const result = cloneDeep(elements);

  elements.forEach((element, index) => {
    Object.keys(element).forEach(key => {
      const value = fields.find(field => TextUtils.areEquals(field['name'], element[key]));
      result[index][key] = value ? value : element[key];
    });
  });

  return result || [];
};

const parseFields = (dataFields = [], schemaTables) => {
  return dataFields.map(field => {
    const schemaField = schemaTables.records[0].fields.find(
      element =>
        !isNil(element['fieldId']) &&
        !isNil(field['fieldSchemaId']) &&
        TextUtils.areEquals(element['fieldId'], field['fieldSchemaId'])
    );

    return {
      codelistItems: schemaField.codelistItems,
      description: schemaField.description,
      fieldId: field.fieldId,
      fieldSchemaId: field.fieldSchemaId,
      label: schemaField.label,
      maxSize: schemaField.maxSize,
      name: schemaField.name,
      pk: schemaField.pk,
      pkHasMultipleValues: schemaField.pkHasMultipleValues,
      pkMustBeUsed: schemaField.pkMustBeUsed,
      pkReferenced: schemaField.pkReferenced,
      readOnly: schemaField.readOnly,
      recordId: field.recordId,
      referencedField: schemaField.referencedField,
      required: schemaField.required,
      type: field.type,
      unique: schemaField.unique,
      validations: schemaField.validations || field.validations,
      validExtensions: schemaField.validExtensions,
      value: field.value
    };
  });
};

export const NationalSystemsTableUtils = { parseData };
