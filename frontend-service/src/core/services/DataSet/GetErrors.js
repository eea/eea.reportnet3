export const GetErrors = ({ datasetRepository }) => async (datasetId, pageNum, pageSize, sortField, asc) =>
  datasetRepository.errorsById(datasetId, pageNum, pageSize, sortField, asc);
