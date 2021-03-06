import intersection from 'lodash/intersection';
import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isString from 'lodash/isString';
import isUndefined from 'lodash/isUndefined';

import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

const allAttachments = colsSchema => {
  const notAttachment = colsSchema.filter(col => !TextUtils.areEquals(col.type, 'ATTACHMENT'));
  return notAttachment.length === 0;
};

const changeCellValue = (tableData, rowIndex, field, value) => {
  tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
  return tableData;
};

const changeRecordValue = (recordData, field, value, isPaste = false) => {
  //Delete \r and \n values for tabular paste
  if (isPaste && !isUndefined(value) && !isNull(value) && isString(value)) {
    value = value.replace(`\r`, '').replace(`\n`, '');
  }
  recordData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
  return recordData;
};
const changeRecordInTable = (tableData, rowIndex, colsSchema, records) => {
  let record = tableData[rowIndex];
  const recordFiltered = RecordUtils.getInitialRecordValues(record, colsSchema);
  if (!isEqual(recordFiltered.flat(), records.initialRecordValue.flat())) {
    for (let i = 0; i < records.initialRecordValue.length; i++) {
      record = RecordUtils.changeRecordValue(
        record,
        records.initialRecordValue[i][0],
        records.initialRecordValue[i][1]
      );
    }
    tableData[rowIndex] = record;
    return tableData;
  }
};

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

const getCellFieldSchemaId = (tableData, field) => {
  const completeField = tableData.rowData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0];
  return !isUndefined(completeField) ? completeField.fieldData.fieldSchemaId : undefined;
};

const getCellId = (tableData, field) => {
  const completeField = tableData.rowData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0];
  return !isUndefined(completeField) ? completeField.fieldData.id : undefined;
};

const getCellInfo = (colSchemaData, field) => {
  const completeField = colSchemaData.filter(data => data.field === field)[0];
  return !isUndefined(completeField) ? completeField : undefined;
};

const getCellItems = (colSchemaData, field) => {
  const completeField = colSchemaData.filter(data => data.field === field)[0];
  return !isUndefined(completeField) ? completeField.codelistItems : undefined;
};

const getCellValue = (tableData, field) => {
  const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
  return value.length > 0
    ? value[0].fieldData[field]?.type === 'MULTISELECT_CODELIST'
      ? value[0].fieldData[field]?.join(';')
      : value[0].fieldData[field]
    : '';
};

const getClipboardData = (pastedData, pastedRecords, colsSchema, fetchedDataFirstRow, reporting) => {
  //Delete double quotes from strings
  const copiedClipboardRecords = pastedData
    .split('\n')
    .filter(l => l.length > 0)
    .map(d => d.replace(/["]+/g, '').replace('\n', ' '));
  //Maximum number of records to paste should be 500
  const copiedBulkRecords = !isUndefined(pastedRecords) ? [...pastedRecords].slice(0, 500) : [];

  const readOnlyFieldsIndex = [];
  colsSchema.forEach((col, i) => {
    if (col.readOnly) {
      readOnlyFieldsIndex.push(i);
    }
  });

  copiedClipboardRecords.forEach(row => {
    let emptyRecord = RecordUtils.createEmptyObject(colsSchema, fetchedDataFirstRow);
    const copiedCols = row.split('\t');

    emptyRecord.dataRow.forEach((record, i) => {
      emptyRecord = RecordUtils.changeRecordValue(
        emptyRecord,
        record.fieldData.fieldSchemaId,
        (readOnlyFieldsIndex.indexOf(i) > -1 && reporting) || record.fieldData.type === 'ATTACHMENT'
          ? ''
          : copiedCols[i],
        true
      );
    });

    emptyRecord.dataRow = emptyRecord.dataRow.filter(
      column => Object.keys(column.fieldData)[0] !== 'id' && Object.keys(column.fieldData)[0] !== 'datasetPartitionId'
    );
    emptyRecord.copiedCols = copiedCols.length;
    copiedBulkRecords.push(emptyRecord);
  });
  //Slice to 500 records and renumber de records for delete button
  return copiedBulkRecords.slice(0, 500).map((record, i) => {
    return { ...record, recordId: i };
  });
};

const getCodelistItems = (colsSchema, field) => {
  const codelistItems = getCellItems(colsSchema, field);
  return !isNil(codelistItems)
    ? codelistItems
        .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
        .map(codelistItem => {
          return { itemType: codelistItem, value: codelistItem };
        })
    : [];
};

const getCodelistItemsInSingleColumn = column => {
  const codelistItems = column.codelistItems;
  return !isNil(codelistItems)
    ? codelistItems
        .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
        .map(codelistItem => {
          return { itemType: codelistItem, value: codelistItem };
        })
    : [];
};

const getCodelistItemsWithEmptyOption = (column, noneText) => {
  const codelistItems = column.codelistItems
    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
    .map(codelistItem => {
      return { itemType: codelistItem, value: codelistItem };
    });

  codelistItems.unshift({
    itemType: noneText,
    value: ''
  });
  return codelistItems;
};

const getCodelistValue = (codelistItemsOptions, value) => {
  if (!isUndefined(value)) {
    return codelistItemsOptions.filter(item => item.value === value)[0];
  }
};

const getFieldTypeValue = fieldType => {
  const fieldTypes = [
    { fieldType: 'Number_Integer', value: 'Number - Integer' },
    { fieldType: 'Number_Decimal', value: 'Number - Decimal' },
    { fieldType: 'Date', value: 'Date' },
    { fieldType: 'Datetime', value: 'Datetime' },
    { fieldType: 'Text', value: 'Text' },
    // { fieldType: 'Rich_Text', value: 'Rich text' },
    { fieldType: 'Textarea', value: 'Multiline text' },
    { fieldType: 'Email', value: 'Email' },
    { fieldType: 'URL', value: 'URL' },
    { fieldType: 'Phone', value: 'Phone number' },
    { fieldType: 'Point', value: 'Point' },
    { fieldType: 'MultiPoint', value: 'Multiple points' },
    { fieldType: 'Linestring', value: 'Line' },
    { fieldType: 'MultiLineString', value: 'Multiple lines' },
    { fieldType: 'Polygon', value: 'Polygon' },
    { fieldType: 'MultiPolygon', value: 'Multiple polygons', fieldTypeIcon: 'multiPolygon' },
    { fieldType: 'Codelist', value: 'Single select' },
    { fieldType: 'Multiselect_Codelist', value: 'Multiple select' },
    { fieldType: 'Link', value: 'Link' },
    { fieldType: 'External_link', value: 'External link' },
    { fieldType: 'Attachment', value: 'Attachment' }
  ];

  if (!isUndefined(fieldType)) {
    const filteredTypes = fieldTypes.filter(field => TextUtils.areEquals(field.fieldType, fieldType))[0];
    return filteredTypes.value;
  } else {
    return '';
  }
};

const getFilter = type => {
  switch (type) {
    case 'NUMBER_INTEGER':
      return 'int';
    case 'NUMBER_DECIMAL':
    case 'POINT':
      return 'coordinates';
    case 'DATE':
      return 'date';
    case 'TEXT':
    case 'RICH_TEXT':
    case 'TEXTAREA':
      return 'any';
    case 'EMAIL':
      return 'email';
    case 'PHONE':
      return 'phone';
    // case 'URL':
    //   return 'url';
    default:
      return 'any';
  }
};

const getInitialRecordValues = (record, colsSchema) => {
  const initialValues = [];
  const filteredColumns = colsSchema.filter(
    column =>
      column.key !== 'actions' &&
      column.key !== 'recordValidation' &&
      column.key !== 'id' &&
      column.key !== 'datasetPartitionId'
  );
  filteredColumns.forEach(column => {
    if (!isUndefined(record.dataRow)) {
      const field = record.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      if (!isUndefined(field)) initialValues.push([column.field, field.fieldData[column.field]]);
    }
  });
  return initialValues;
};

const getLinkValue = (linkOptions, value) => {
  if (!isUndefined(value) && !isUndefined(linkOptions)) {
    return linkOptions.filter(item => item.value === value)[0];
  }
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

const getNumCopiedRecords = pastedData => {
  if (!isUndefined(pastedData)) {
    const copiedClipboardRecords = pastedData
      .split('\r\n')
      .filter(l => l.length > 0)
      .map(d => d.replace(/["]+/g, '').replace('\n', ' '));
    return copiedClipboardRecords.length;
  } else {
    return 0;
  }
};

const getRecordId = (tableData, record) => {
  return tableData
    .map(e => {
      return e.recordId;
    })
    .indexOf(record.recordId);
};

const getTextWidth = (text, font) => {
  const canvas =
    RecordUtils.getTextWidth.canvas || (RecordUtils.getTextWidth.canvas = document.createElement('canvas'));
  const context = canvas.getContext('2d');
  context.font = font;
  const metrics = context.measureText(text);
  return Number(metrics.width);
};

const createEmptyObject = (columnsSchema, data) => {
  let fields;
  if (!isUndefined(columnsSchema)) {
    fields = columnsSchema.map(column => {
      return {
        fieldData: { [column.field]: null, type: column.type, fieldSchemaId: column.field }
      };
    });
  }
  const obj = {
    dataRow: fields,
    recordSchemaId: columnsSchema[0].recordId
  };

  obj.datasetPartitionId = null;
  //dataSetPartitionId is needed for checking the rows owned by delegated contributors
  if (!isUndefined(data) && data.length > 0) {
    obj.datasetPartitionId = data.datasetPartitionId;
  }

  return obj;
};

export const RecordUtils = {
  allAttachments,
  changeCellValue,
  changeRecordInTable,
  changeRecordValue,
  createEmptyObject,
  formatDate,
  getCellFieldSchemaId,
  getCellId,
  getCellInfo,
  getCellItems,
  getCellValue,
  getClipboardData,
  getCodelistItems,
  getCodelistItemsInSingleColumn,
  getCodelistItemsWithEmptyOption,
  getCodelistValue,
  getFieldTypeValue,
  getFilter,
  getInitialRecordValues,
  getLinkValue,
  getMultiselectValues,
  getNumCopiedRecords,
  getRecordId,
  getTextWidth
};
