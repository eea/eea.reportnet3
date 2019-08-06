export const GetDataSetErrors = ({ dataSetRepository }) => async dataSetId => dataSetRepository.errorsById(dataSetId);
