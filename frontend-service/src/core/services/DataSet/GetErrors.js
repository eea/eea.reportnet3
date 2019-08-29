export const GetErrors = ({ dataSetRepository }) => async (dataSetId, pageNum, pageSize, sortField, asc) =>
  dataSetRepository.errorsById(dataSetId, pageNum, pageSize, sortField, asc);
