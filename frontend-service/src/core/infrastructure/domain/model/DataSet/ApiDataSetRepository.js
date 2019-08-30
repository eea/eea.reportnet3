import isNull from 'lodash/isNull';

import { apiDataSet } from 'core/infrastructure/api/domain/model/DataSet';
import { DataSetError } from 'core/domain/model/DataSet/DataSetError/DataSetError';
import { DataSet } from 'core/domain/model/DataSet/DataSet';
import { DataSetTable } from 'core/domain/model/DataSet/DataSetTable/DataSetTable';
import { DataSetTableField } from 'core/domain/model/DataSet/DataSetTable/DataSetRecord/DataSetTableField/DataSetTableField';
import { DataSetTableRecord } from 'core/domain/model/DataSet/DataSetTable/DataSetRecord/DataSetTableRecord';
import { Validation } from 'core/domain/model/Validation/Validation';

const addRecordById = async (dataSetId, tableSchemaId, record) => {
  console.log(record);
  const dataSetTableRecord = new DataSetTableRecord();

  const recordAdded = await apiDataSet.addRecordById(dataSetId, tableSchemaId, record);
  return recordAdded;
};

const deleteDataById = async dataSetId => {
  const dataDeleted = await apiDataSet.deleteDataById(dataSetId);
  return dataDeleted;
};

const deleteRecordByIds = async (dataSetId, recordIds) => {
  const recordDeleted = await apiDataSet.deleteRecordByIds(dataSetId, recordIds);
  return recordDeleted;
};

const deleteTableDataById = async (dataSetId, tableId) => {
  const dataDeleted = await apiDataSet.deleteTableDataById(dataSetId, tableId);
  return dataDeleted;
};

const errorsById = async (dataSetId, pageNum, pageSize, sortField, asc) => {
  const dataSetErrorsDTO = await apiDataSet.errorsById(dataSetId, pageNum, pageSize, sortField, asc);

  const dataSet = new DataSet(
    null,
    dataSetErrorsDTO.idDataset,
    dataSetErrorsDTO.idDatasetSchema,
    dataSetErrorsDTO.nameDataSetSchema,
    dataSetErrorsDTO.totalErrors
  );

  const errors = dataSetErrorsDTO.errors.map(
    dataSetErrorDTO =>
      new DataSetError(
        dataSetErrorDTO.typeEntity,
        dataSetErrorDTO.levelError,
        dataSetErrorDTO.message,
        dataSetErrorDTO.idObject,
        null,
        null,
        dataSetErrorDTO.idTableSchema,
        dataSetErrorDTO.nameTableSchema,
        dataSetErrorDTO.validationDate,
        dataSetErrorDTO.idValidation
      )
  );

  dataSet.errors = errors;

  return dataSet;
};

const errorPositionByObjectId = async (objectId, dataSetId, entityType) => {
  const dataSetErrorDTO = await apiDataSet.errorPositionByObjectId(objectId, dataSetId, entityType);

  const dataSetError = new DataSetError();
  dataSetError.position = dataSetErrorDTO.position;
  dataSetError.recordId = dataSetErrorDTO.idRecord;
  dataSetError.tableSchemaId = dataSetErrorDTO.idTableSchema;
  dataSetError.tableSchemaName = dataSetErrorDTO.nameTableSchema;

  return dataSetError;
};

const errorStatisticsById = async dataSetId => {
  const dataSetTablesDTO = await apiDataSet.statisticsById(dataSetId);

  const dataSet = new DataSet();
  dataSet.dataSetSchemaName = dataSetTablesDTO.nameDataSetSchema;
  dataSet.datasetErrors = dataSetTablesDTO.datasetErrors;

  const tableStatisticValues = [];

  const dataSetTables = dataSetTablesDTO.tables.map(dataSetTableDTO => {
    tableStatisticValues.push([
      dataSetTableDTO.totalRecords -
        (dataSetTableDTO.totalRecordsWithErrors + dataSetTableDTO.totalRecordsWithWarnings),
      dataSetTableDTO.totalRecordsWithWarnings,
      dataSetTableDTO.totalRecordsWithErrors
    ]);

    return new DataSetTable(
      dataSetTableDTO.tableErrors,
      dataSetTableDTO.idTableSchema,
      dataSetTableDTO.nameTableSchema
    );
  });

  //Transpose value matrix to fit Chart data structure
  let transposedValues = transposeMatrix(tableStatisticValues);

  dataSet.tableStatisticValues = tableStatisticValues;
  dataSet.tableStatisticPercentages = getPercentage(transposedValues);

  dataSet.tables = dataSetTables;

  return dataSet;
};

const exportDataById = async (dataSetId, fileType) => {
  const dataSetData = await apiDataSet.exportDataById(dataSetId, fileType);
  return dataSetData;
};

const exportTableDataById = async (dataSetId, tableSchemaId, fileType) => {
  const dataSetTableData = await apiDataSet.exportTableDataById(dataSetId, tableSchemaId, fileType);
  return dataSetTableData;
};

const schemaById = async dataFlowId => {
  const dataSetSchemaDTO = await apiDataSet.schemaById(dataFlowId);

  const dataSet = new DataSet();
  dataSet.dataSetSchemaId = dataSetSchemaDTO.idDatasetSchema;
  dataSet.dataSetSchemaName = dataSetSchemaDTO.nameDataSetSchema;

  const tables = dataSetSchemaDTO.tableSchemas.map(dataSetTableDTO => {
    const records = [dataSetTableDTO.recordSchema].map(dataTableRecordDTO => {
      const fields = dataTableRecordDTO.fieldSchema.map(DataTableFieldDTO => {
        return new DataSetTableField(
          DataTableFieldDTO.id,
          DataTableFieldDTO.idRecord,
          DataTableFieldDTO.name,
          DataTableFieldDTO.type
        );
      });
      return new DataSetTableRecord(dataTableRecordDTO.id, dataTableRecordDTO.idRecordSchema, fields);
    });
    return new DataSetTable(
      null,
      dataSetTableDTO.idTableSchema,
      dataSetTableDTO.nameTableSchema,
      null,
      null,
      null,
      records
    );
  });

  dataSet.tables = tables;

  return dataSet;
};

const tableDataById = async (dataSetId, tableSchemaId, pageNum, pageSize, fields) => {
  const tableDataDTO = await apiDataSet.tableDataById(dataSetId, tableSchemaId, pageNum, pageSize, fields);
  const table = new DataSetTable();

  if (tableDataDTO.totalRecords > 0) {
    table.tableSchemaId = tableDataDTO.idTableSchema;
    table.totalRecords = tableDataDTO.totalRecords;

    let field, record;

    const records = tableDataDTO.records.map(dataTableRecordDTO => {
      record = new DataSetTableRecord();
      const fields = dataTableRecordDTO.fields.map(DataTableFieldDTO => {
        field = new DataSetTableField();
        field.fieldId = DataTableFieldDTO.id;
        field.fieldSchemaId = DataTableFieldDTO.idFieldSchema;
        field.recordId = dataTableRecordDTO.idRecordSchema;
        field.name = DataTableFieldDTO.name;
        field.type = DataTableFieldDTO.type;
        field.value = DataTableFieldDTO.value;

        if (!isNull(DataTableFieldDTO.fieldValidations)) {
          field.validations = DataTableFieldDTO.fieldValidations.map(fieldValidation => {
            return new Validation(
              fieldValidation.id,
              fieldValidation.validation.levelError,
              fieldValidation.validation.typeEntity,
              fieldValidation.validation.validationDate,
              fieldValidation.validation.message
            );
          });
        }
        return field;
      });

      record.recordId = dataTableRecordDTO.id;
      record.recordSchemaId = dataTableRecordDTO.idRecordSchema;
      record.fields = fields;

      if (!isNull(dataTableRecordDTO.recordValidations)) {
        record.validations = dataTableRecordDTO.recordValidations.map(recordValidation => {
          return new Validation(
            recordValidation.id,
            recordValidation.validation.levelError,
            recordValidation.validation.typeEntity,
            recordValidation.validation.validationDate,
            recordValidation.validation.message
          );
        });
      }
      return record;
    });

    table.records = records;
  }
  return table;
};

const webFormDataById = async (dataSetId, tableSchemaId) => {
  const webFormDataDTO = await apiDataSet.webFormDataById(dataSetId, tableSchemaId);
  const webForm = new DataSetTable();

  if (webFormDataDTO.totalRecords > 0) {
    webForm.tableSchemaId = webFormDataDTO.idTableSchema;
    webForm.totalRecords = webFormDataDTO.totalRecords;

    let field, record;

    const records = webFormDataDTO.records.map(webFormRecordDTO => {
      record = new DataSetTableRecord();
      const fields = webFormRecordDTO.fields.map(webFormFieldDTO => {
        field = new DataSetTableField();
        field.fieldId = webFormFieldDTO.id;
        field.fieldSchemaId = webFormFieldDTO.idFieldSchema;
        field.recordId = webFormRecordDTO.idRecordSchema;
        field.name = webFormFieldDTO.name;
        field.type = webFormFieldDTO.type;
        field.value = webFormFieldDTO.value;

        return field;
      });

      record.recordId = webFormRecordDTO.id;
      record.recordSchemaId = webFormRecordDTO.idRecordSchema;
      record.fields = fields;

      return record;
    });
    webForm.records = records;
  }
  return webForm;
};

const updateFieldById = async (dataSetId, fieldSchemaId, fieldId, fieldType, fieldValue) => {
  const dataSetTableField = new DataSetTableField();
  //dataSetTableField.fieldId = fieldId;
  //dataSetTableField.fieldSchemaId = fieldSchemaId;
  dataSetTableField.id = fieldId;
  dataSetTableField.idFieldSchema = fieldSchemaId;
  dataSetTableField.type = fieldType;
  dataSetTableField.value = fieldValue;

  console.log(JSON.stringify(dataSetTableField));

  const fieldUpdated = await apiDataSet.updateFieldById(dataSetId, dataSetTableField);
  return fieldUpdated;
};

const validateDataById = async dataSetId => {
  const dataValidation = await apiDataSet.validateById(dataSetId);
  return dataValidation;
};

const getPercentage = valArr => {
  let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
  return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
};

const transposeMatrix = matrix => {
  return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
};

export const ApiDataSetRepository = {
  addRecordById,
  deleteDataById,
  deleteRecordByIds,
  deleteTableDataById,
  errorsById,
  errorPositionByObjectId,
  errorStatisticsById,
  exportDataById,
  exportTableDataById,
  schemaById,
  tableDataById,
  updateFieldById,
  validateDataById,
  webFormDataById
};
