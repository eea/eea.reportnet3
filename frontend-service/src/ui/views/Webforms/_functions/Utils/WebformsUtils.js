import compact from 'lodash/compact';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const getWebformTabs = (allTables = [], schemaTables, configTables = {}, selectedValue) => {
  const initialValues = {};

  let tableIdx = 0;
  if (QuerystringUtils.getUrlParamValue('tab') !== '') {
    const filteredTable = schemaTables.filter(
      schemaTable => schemaTable.id === QuerystringUtils.getUrlParamValue('tab')
    );
    if (!isEmpty(filteredTable)) {
      tableIdx = allTables.indexOf(filteredTable[0].name);
    }
    //Search on subtables for parent id
    if (tableIdx === -1) {
      configTables.forEach(table => {
        table.elements.forEach(element => {
          if (element.type === 'TABLE') {
            if (element.name === filteredTable[0].name && element.name !== 'PaMs') {
              tableIdx = allTables.indexOf(table.name);
            }
          }
        });
      });
    }
  }
  const value = allTables[tableIdx === -1 ? 0 : tableIdx];

  compact(allTables).forEach(table => {
    initialValues[table] = false;
    initialValues[selectedValue ? selectedValue : value] = true;
  });

  return initialValues;
};

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

const getSchemaIdForIdSectorObjectivesField = records => {
  let isFound = false;
  let id_SectorObjectives_FieldSchemaId;
  records.forEach(record => {
    if (!isEmpty(record.fields) && !isFound) {
      const { fields } = record;

      fields.forEach(field => {
        if (field.name === 'Id_SectorObjectives' && !isFound) {
          id_SectorObjectives_FieldSchemaId = field.fieldSchema;
          isFound = true;
        }
      });
    }
  });
  return id_SectorObjectives_FieldSchemaId;
};

const findMaximumIdValue = (records, SectorObjectivesTable) => {
  let fieldsIds = [];

  let id_SectorObjectives_FieldSchemaId = getSchemaIdForIdSectorObjectivesField(records);

  if (id_SectorObjectives_FieldSchemaId && SectorObjectivesTable) {
    SectorObjectivesTable.elementsRecords.forEach(record => {
      record.fields.forEach(field => {
        if (field.fieldSchemaId === id_SectorObjectives_FieldSchemaId) {
          fieldsIds.push(field.value);
        }
      });
    });
  }

  fieldsIds = fieldsIds
    .filter(fieldId => !isNil(fieldId) && fieldId !== '')
    .map(fieldId => fieldId.split('_')[1])
    .map(fieldId => parseInt(fieldId));

  if (isEmpty(fieldsIds)) {
    return 1;
  }

  return Math.max(...fieldsIds);
};

const parseNewTableRecord = (table, pamNumber, SectorObjectivesTable) => {
  if (!isNil(table) && !isNil(table.records) && !isEmpty(table.records)) {
    let fields;
    let id_SectorObjectives_FieldSchemaId = getSchemaIdForIdSectorObjectivesField(table.records);

    if (!isUndefined(table)) {
      fields = table.records[0].fields.map(field => {
        return {
          fieldData: {
            [field.fieldSchema || field.fieldId]: TextUtils.areEquals(field.name, 'FK_PAMS')
              ? pamNumber
              : field.fieldSchema === id_SectorObjectives_FieldSchemaId ||
                field.fieldId === id_SectorObjectives_FieldSchemaId
              ? `${pamNumber}_${findMaximumIdValue(table.records, SectorObjectivesTable) + 1}`
              : null,
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

const parsePamsRecords = records =>
  records.map(record => {
    const { recordId, recordSchemaId } = record;
    let data = {};

    record.elements.forEach(
      element => (data = { ...data, [element.name]: element.value, recordId: recordId, recordSchemaId: recordSchemaId })
    );

    return data;
  });

export const WebformsUtils = {
  getWebformTabs,
  mergeArrays,
  onParseWebformData,
  onParseWebformRecords,
  parseNewTableRecord,
  parsePamsRecords
};
