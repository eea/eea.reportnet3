import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isString from 'lodash/isString';
import isUndefined from 'lodash/isUndefined';

const changeCellValue = (tableData, rowIndex, field, value) => {
  tableData[rowIndex].dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
  return tableData;
};

const changeRecordValue = (recordData, field, value) => {
  //Delete \r and \n values for tabular paste
  if (!isUndefined(value) && !isNull(value) && isString(value)) {
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

const getCellFieldSchemaId = (tableData, field) => {
  const completeField = tableData.rowData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0];
  return !isUndefined(completeField) ? completeField.fieldData.fieldSchemaId : undefined;
};

const getCellId = (tableData, field) => {
  const completeField = tableData.rowData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0];
  return !isUndefined(completeField) ? completeField.fieldData.id : undefined;
};

const getCellItems = (colSchemaData, field) => {
  const completeField = colSchemaData.filter(data => data.field === field)[0];
  return !isUndefined(completeField) ? completeField.codelistItems : undefined;
};

const getCellValue = (tableData, field) => {
  const value = tableData.rowData.dataRow.filter(data => data.fieldData[field]);
  return value.length > 0 ? value[0].fieldData[field] : '';
};

const getClipboardData = (pastedData, pastedRecords, colsSchema, fetchedDataFirstRow) => {
  //Delete double quotes from strings
  const copiedClipboardRecords = pastedData
    .split('\r\n')
    .filter(l => l.length > 0)
    .map(d => d.replace(/["]+/g, '').replace('\n', ' '));
  //Maximum number of records to paste should be 500
  const copiedBulkRecords = !isUndefined(pastedRecords) ? [...pastedRecords].slice(0, 500) : [];
  copiedClipboardRecords.forEach(row => {
    let emptyRecord = RecordUtils.createEmptyObject(colsSchema, fetchedDataFirstRow);
    const copiedCols = row.split('\t');
    emptyRecord.dataRow.forEach((record, i) => {
      emptyRecord = RecordUtils.changeRecordValue(emptyRecord, record.fieldData.fieldSchemaId, copiedCols[i]);
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
    ? codelistItems.map(codelistItem => {
        return { itemType: codelistItem, value: codelistItem };
      })
    : [];
};

const getCodelistItemsInSingleColumn = column => {
  const codelistItems = column.codelistItems;
  return !isNil(codelistItems)
    ? codelistItems.map(codelistItem => {
        return { itemType: codelistItem, value: codelistItem };
      })
    : [];
};

const getCodelistValue = (codelistItemsOptions, value) => {
  if (!isUndefined(value)) {
    return codelistItemsOptions.filter(item => item.value === value)[0];
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
  console.log(
    { multiselectItemsOptions, value },
    multiselectItemsOptions.filter(item => item.value === value[0])
  );
  if (!isUndefined(value) && !isUndefined(value[0])) {
    return multiselectItemsOptions.filter(item => value.includes(item.value)).map(item => item.value);
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
  changeCellValue,
  changeRecordInTable,
  changeRecordValue,
  createEmptyObject,
  getCellFieldSchemaId,
  getCellId,
  getCellItems,
  getCellValue,
  getClipboardData,
  getCodelistItems,
  getCodelistItemsInSingleColumn,
  getCodelistValue,
  getInitialRecordValues,
  getLinkValue,
  getMultiselectValues,
  getNumCopiedRecords,
  getRecordId,
  getTextWidth
};
