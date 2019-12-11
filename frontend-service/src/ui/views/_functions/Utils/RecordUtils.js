import { isUndefined, isNull, isString, isEmpty, capitalize } from 'lodash';

export const RecordUtils = {
  changeRecordValue: (recordData, field, value) => {
    //Delete \r and \n values for tabular paste
    if (!isUndefined(value) && !isNull(value) && isString(value)) {
      value = value.replace(`\r`, '').replace(`\n`, '');
    }
    recordData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
    return recordData;
  },
  getClipboardData: (pastedData, pastedRecords, colsSchema, fetchedDataFirstRow) => {
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
  },
  getInitialRecordValues: (record, colsSchema) => {
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
        initialValues.push([column.field, field.fieldData[column.field]]);
      }
    });
    return initialValues;
  },
  getNumCopiedRecords: pastedData => {
    if (!isUndefined(pastedData)) {
      const copiedClipboardRecords = pastedData
        .split('\r\n')
        .filter(l => l.length > 0)
        .map(d => d.replace(/["]+/g, '').replace('\n', ' '));
      return copiedClipboardRecords.length;
    } else {
      return 0;
    }
  },
  getTextWidth: (text, font) => {
    const canvas =
      RecordUtils.getTextWidth.canvas || (RecordUtils.getTextWidth.canvas = document.createElement('canvas'));
    const context = canvas.getContext('2d');
    context.font = font;
    const metrics = context.measureText(text);
    return Number(metrics.width);
  },
  createEmptyObject: (columnsSchema, data) => {
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
  }
};
