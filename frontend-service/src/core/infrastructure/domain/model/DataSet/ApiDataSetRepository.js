import { api } from 'core/infrastructure/api';
import { DataSetError } from 'core/domain/model/DataSet/DataSetError/DataSetError';
import { DataSet } from 'core/domain/model/DataSet/DataSet';

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

export const ApiDataSetRepository = {
  errorsById,
  errorPositionByObjectId
};
