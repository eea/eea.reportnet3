import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

const getFormInitialValues = fields => {
  const initialValues = {};

  fields.forEach(field => {
    initialValues[field.fieldId] = {
      fieldId: field.fieldId,
      fieldSchemaId: field.fieldId,
      fieldName: field.fieldName,
      fieldType: field.fieldType,
      newValue: '',
      recordId: field.recordId,
      required: field.required,
      value: ''
    };
  });

  return initialValues;
};

const parseNewRecordData = (columnsSchema = [{ recordId: null }], data) => {
  if (!isEmpty(columnsSchema)) {
    let fields;

    if (!isUndefined(columnsSchema)) {
      fields = columnsSchema.map(column => {
        return {
          fieldData: { [column.fieldId]: null, type: column.type, fieldSchemaId: column.fieldId }
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

export const WebformRecordUtils = { getFormInitialValues, parseNewRecordData };
