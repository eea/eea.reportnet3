import { api } from 'core/infrastructure/api';
import { DataSetError } from 'core/domain/model/DataSet/DataSetError/DataSetError';
import { DataSet } from 'core/domain/model/DataSet/DataSet';
import { DataSetTable } from 'core/domain/model/DataSet/DataSetTable/DataSetTable';
import { DataSetTableField } from 'core/domain/model/DataSet/DataSetTable/DataSetRecord/DataSetTableField/DataSetTableField';
import { DataSetTableRecord } from 'core/domain/model/DataSet/DataSetTable/DataSetRecord/DataSetTableRecord';

const dataSetSchemaById = async dataFlowId => {
  const dataSetSchemaDTO = await api.dataSetSchemaById(dataFlowId);

  const dataSet = new DataSet();
  dataSet.dataSetSchemaId = dataSetSchemaDTO.idDatasetSchema;
  dataSet.dataSetSchemaName = dataSetSchemaDTO.nameDataSetSchema;

  const tables = dataSetSchemaDTO.tableSchemas.map(dataSetTableDTO => {
    const records = [dataSetTableDTO.recordSchema].map(dataSetRecordDTO => {
      const fields = dataSetRecordDTO.fieldSchema.map(dataSetFieldDTO => {
        return new DataSetTableField(
          dataSetFieldDTO.id,
          dataSetFieldDTO.idRecord,
          dataSetFieldDTO.name,
          dataSetFieldDTO.type
        );
      });
      return new DataSetTableRecord(dataSetRecordDTO.idRecordSchema, fields);
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

const deleteDataById = async dataSetId => {
  const dataDeleted = await api.deleteDataSetDataById(dataSetId);
  return dataDeleted;
};

const errorsById = async (dataSetId, pageNum, pageSize, sortField, asc) => {
  const dataSetErrorsDTO = await api.dataSetErrorsById(dataSetId, pageNum, pageSize, sortField, asc);

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
  const dataSetErrorDTO = await api.errorPositionByObjectId(objectId, dataSetId, entityType);

  const dataSetError = new DataSetError();
  dataSetError.position = dataSetErrorDTO.position;
  dataSetError.recordId = dataSetErrorDTO.idRecord;
  dataSetError.tableSchemaId = dataSetErrorDTO.idTableSchema;
  dataSetError.tableSchemaName = dataSetErrorDTO.nameTableSchema;

  return dataSetError;
};

const errorStatisticsById = async dataSetId => {
  const dataSetTablesDTO = await api.dataSetStatisticsById(dataSetId);

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

  //Transpose value matrix and delete undefined elements to fit Chart data structure
  let transposedValues = transposeMatrix(tableStatisticValues);

  dataSet.tableStatisticValues = tableStatisticValues;
  dataSet.tableStatisticPercentages = getPercentage(transposedValues);

  dataSet.tables = dataSetTables;

  return dataSet;
};

const validateDataById = async dataSetId => {
  const dataValidation = await api.validateDataSetById(dataSetId);
  return dataValidation;
};

const getPercentage = valArr => {
  let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
  return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
};

const transposeMatrix = matrix => {
  return Object.keys(matrix)
    .map(c => matrix.map(r => r[c]))
    .filter(t => t[0] !== undefined);
};

export const ApiDataSetRepository = {
  dataSetSchemaById,
  deleteDataById,
  errorsById,
  errorPositionByObjectId,
  errorStatisticsById,
  validateDataById
};
