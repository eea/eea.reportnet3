import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const mergeArrays = (array1 = [], array2 = [], array1Key = '', array2Key = '') => {
  const result = [];
  for (let i = 0; i < array1.length; i++) {
    result.push({
      ...array1[i],
      ...array2.find(
        element =>
          !isNil(element[array2Key]) &&
          !isNil(array1[i][array1Key]) &&
          TextUtils.areEquals(element[array2Key], array1[i][array1Key])
      )
    });
  }
  return result;
};

const parseNewRecord = (columnsSchema, data) => {
  if (!isEmpty(columnsSchema)) {
    let fields;

    if (!isUndefined(columnsSchema)) {
      fields = columnsSchema.map(column => {
        if (column.type === 'FIELD') {
          return {
            fieldData: { [column.fieldSchema]: null, type: column.fieldType, fieldSchemaId: column.fieldSchema }
          };
        }
      });
    }

    const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

    obj.datasetPartitionId = null;
    if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

    return obj;
  }
};

const parseNewTableRecord = (table, pamNumber) => {
  if (!isNil(table) && !isNil(table.records) && !isEmpty(table.records)) {
    let fields;

    if (!isUndefined(table)) {
      fields = table.records[0].fields.map(field => {
        return {
          fieldData: {
            [field.fieldSchema || field.fieldId]: TextUtils.areEquals(field.name, 'FK_PAMS') ? pamNumber : null,
            type: field.type,
            fieldSchemaId: field.fieldSchema || field.fieldId
          }
        };
      });
    }

    const obj = { dataRow: fields, recordSchemaId: table.recordSchemaId };

    obj.datasetPartitionId = null;
    return obj;
  }
};

const onParseWebformRecords = (records, webform, tableData, totalRecords) => {
  return records.map(record => {
    const { fields } = record;
    const { elements } = webform;

    const result = [];

    for (let index = 0; index < elements.length; index++) {
      const element = elements[index];

      if (element.type === 'FIELD') {
        result.push({
          fieldType: 'EMPTY',
          ...element,
          ...fields.find(field => field['fieldSchemaId'] === element['fieldSchema']),
          codelistItems: element.codelistItems || [],
          description: element.description || '',
          isDisabled: isNil(element.fieldSchema),
          maxSize: element.maxSize,
          name: element.name,
          pk: element.pk,
          pkHasMultipleValues: element.pkHasMultipleValues,
          pkMustBeUsed: element.pkMustBeUsed,
          pkReferenced: element.pkReferenced,
          recordId: record.recordId,
          referencedField: element.referencedField,
          required: element.required,
          type: element.type,
          validExtensions: element.validExtensions
        });
      } else if (element.type === 'BLOCK') {
        result.push({
          ...element,
          elementsRecords: onParseWebformRecords(records, { elements: element.elements }, tableData, totalRecords)
        });
      } else {
        if (tableData[element.tableSchemaId]) {
          const tableElementsRecords = onParseWebformRecords(
            tableData[element.tableSchemaId].records,
            element,
            tableData,
            totalRecords
          );
          result.push({ ...element, elementsRecords: tableElementsRecords });
        } else {
          result.push({ ...element, tableNotCreated: true, elementsRecords: [] });
        }
      }
    }

    return { ...record, elements: result, totalRecords };
  });
};

const onParseWebformData = (datasetSchema, allTables, schemaTables) => {
  const data = mergeArrays(allTables, schemaTables, 'name', 'tableSchemaName');
  data.map(table => {
    if (table.records) {
      table.records[0].fields = table.records[0].fields.map(field => {
        const { fieldId, recordId, type } = field;

        return { fieldSchema: fieldId, fieldType: type, recordSchemaId: recordId, ...field };
      });
    }
  });

  for (let index = 0; index < data.length; index++) {
    const table = data[index];

    if (table.records) {
      const { elements, records } = table;

      const result = [];
      for (let index = 0; index < elements.length; index++) {
        if (TextUtils.areEquals(elements[index].type, 'FIELD')) {
          result.push({
            ...elements[index],
            ...records[0].fields.find(element => TextUtils.areEquals(element['name'], elements[index]['name'])),
            type: elements[index].type
          });
        }

        if (elements[index].type === 'TABLE') {
          const filteredTable = datasetSchema.tables.filter(table =>
            TextUtils.areEquals(table.tableSchemaName, elements[index].name)
          );
          const parsedTable = onParseWebformData(datasetSchema, [elements[index]], filteredTable);

          result.push({ ...elements[index], ...parsedTable[0], type: elements[index].type });
        }

        if (TextUtils.areEquals(elements[index].type, 'LABEL')) {
          result.push({ ...elements[index] });
        }

        if (TextUtils.areEquals(elements[index].type, 'BLOCK')) {
          const blockedElements = [];

          for (const field of elements[index].elements) {
            blockedElements.push({
              ...field,
              ...records[0].fields.find(element => TextUtils.areEquals(element['name'], field['name'])),
              type: field.type
            });
          }

          result.push({ ...elements[index], elements: blockedElements, records, type: elements[index].type });
        }
      }

      table.elements = result;
    }
  }

  return data;
};

export const WebformsUtils = {
  mergeArrays,
  onParseWebformData,
  onParseWebformRecords,
  parseNewRecord,
  parseNewTableRecord
};
