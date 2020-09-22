import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

// const getFormInitialValues = fields => {
//   const initialValues = {};

//   fields.forEach(field => {
//     initialValues[field.fieldId] = {
//       fieldId: field.fieldSchemaId,
//       fieldName: field.fieldName,
//       fieldSchemaId: field.fieldId,
//       fieldType: field.fieldType,
//       newValue: '',
//       recordId: field.recordSchemaId,
//       recordSchemaId: field.recordId,
//       required: field.required,
//       value: field.value
//     };
//   });

//   return initialValues;
// };

// const getRecordsInitialValues = (records = {}) => {
//   records.webformFields = records.webformFields.map(field => ({
//     fieldId: field.fieldSchemaId,
//     fieldName: field.fieldName,
//     fieldSchemaId: field.fieldId,
//     fieldType: field.fieldType,
//     newValue: '',
//     recordId: field.recordSchemaId,
//     recordSchemaId: field.recordId,
//     required: field.required,
//     value: field.value
//   }));
//   return records;
// };

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

export const WebformRecordUtils = { parseNewRecordData };
