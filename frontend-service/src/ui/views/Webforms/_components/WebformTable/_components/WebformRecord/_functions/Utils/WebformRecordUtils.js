import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

const formatDate = (date, isInvalidDate) => {
  if (isInvalidDate) return '';

  let d = new Date(date),
    month = '' + (d.getMonth() + 1),
    day = '' + d.getDate(),
    year = d.getFullYear();

  if (month.length < 2) month = '0' + month;
  if (day.length < 2) day = '0' + day;

  return [year, month, day].join('-');
};

const getInputMaxLength = { TEXT: 10000, RICH_TEXT: 10000, EMAIL: 256, NUMBER_INTEGER: 20, NUMBER_DECIMAL: 40 };

const getInputType = {
  DATE: 'date',
  NUMBER_DECIMAL: 'any',
  NUMBER_INTEGER: 'init',
  POINT: 'coordinates',
  TEXT: 'text',
  EMAIL: 'email',
  PHONE: 'phone',
  RICH_TEXT: 'any'
};

const getMultiselectValues = (multiselectItemsOptions, value) => {
  if (!isUndefined(value) && !isUndefined(value[0]) && !isUndefined(multiselectItemsOptions)) {
    const splittedValue = !Array.isArray(value) ? value.split(',').map(item => item.trim()) : value;
    return intersection(
      splittedValue,
      multiselectItemsOptions.map(item => item.value)
    );
  }
};

const parseMultiselect = record => {
  record.dataRow.forEach(field => {
    if (
      field.fieldData.type === 'MULTISELECT_CODELIST' ||
      (field.fieldData.type === 'LINK' && Array.isArray(field.fieldData[field.fieldData.fieldSchemaId]))
    ) {
      if (
        !isNil(field.fieldData[field.fieldData.fieldSchemaId]) &&
        field.fieldData[field.fieldData.fieldSchemaId] !== ''
      ) {
        if (Array.isArray(field.fieldData[field.fieldData.fieldSchemaId])) {
          field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId].join(',');
        } else {
          field.fieldData[field.fieldData.fieldSchemaId] = field.fieldData[field.fieldData.fieldSchemaId]
            .split(',')
            .map(item => item.trim())
            .join(',');
        }
      }
    }
  });
  return record;
};

const parseNewRecordData = (columnsSchema, data) => {
  if (!isEmpty(columnsSchema)) {
    let fields;

    if (!isUndefined(columnsSchema)) {
      fields = columnsSchema.map(column => {
        return {
          fieldData: { [column.fieldSchemaId]: null, type: column.type, fieldSchemaId: column.fieldSchemaId }
        };
      });
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
  getInputMaxLength,
  getInputType,
  getMultiselectValues,
  parseMultiselect,
  parseNewRecordData
};
