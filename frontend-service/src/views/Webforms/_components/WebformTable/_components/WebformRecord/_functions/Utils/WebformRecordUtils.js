import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { TextUtils } from 'repositories/_utils/TextUtils';

const formatDate = (date, isInvalidDate) => {
  if (isInvalidDate || date === '') return '';

  let d = new Date(date),
    month = '' + (d.getMonth() + 1),
    day = '' + d.getDate(),
    year = d.getFullYear();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join('-');
};

const getMultiselectValues = (multiselectItemsOptions, value) => {
  if (!isUndefined(value) && !isUndefined(value[0]) && !isUndefined(multiselectItemsOptions)) {
    const splittedValue = !Array.isArray(value) ? TextUtils.splitByChar(value, ';') : value;
    return intersection(
      splittedValue,
      multiselectItemsOptions.map(item => item.value)
    ).sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }));
  }
};

const parseMultiselect = record => {
  record.dataRow.forEach(field => {
    if (
      field.fieldData.type === 'MULTISELECT_CODELIST' ||
      ((field.fieldData.type === 'LINK' || field.fieldData.type === 'EXTERNAL_LINK') &&
        Array.isArray(field.fieldData[field.fieldData.fieldSchemaId]))
    ) {
      if (
        !isNil(field.fieldData[field.fieldData.fieldSchemaId]) &&
        field.fieldData[field.fieldData.fieldSchemaId] !== ''
      ) {
        if (Array.isArray(field.fieldData[field.fieldData.fieldSchemaId])) {
          field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId].join(';');
        } else {
          field.fieldData[field.fieldData.fieldSchemaId] = TextUtils.removeSemicolonSeparatedWhiteSpaces(
            field.fieldData[field.fieldData.fieldSchemaId]
          );
        }
      }
    }
  });
  return record;
};

const parseNewRecordData = (columnsSchema, data) => {
  if (!isEmpty(columnsSchema)) {
    const fields = [];

    if (!isUndefined(columnsSchema)) {
      for (const column of columnsSchema) {
        if (column.type === 'BLOCK') {
          column.elementsRecords[0].elements.forEach(element => {
            fields.push({
              fieldData: { [element.fieldSchemaId]: null, type: element.type, fieldSchemaId: element.fieldSchemaId }
            });
          });
        }
        fields.push({
          fieldData: { [column.fieldSchemaId]: null, type: column.type, fieldSchemaId: column.fieldSchemaId }
        });
      }
    }

    const obj = { dataRow: fields, recordSchemaId: columnsSchema[0].recordId };

    obj.datasetPartitionId = null;
    //dataSetPartitionId is needed for checking the rows owned by delegated contributors
    if (!isUndefined(data) && data.length > 0) obj.datasetPartitionId = data.datasetPartitionId;

    return obj;
  }
};

export const WebformRecordUtils = {
  formatDate,
  getMultiselectValues,
  parseMultiselect,
  parseNewRecordData
};
