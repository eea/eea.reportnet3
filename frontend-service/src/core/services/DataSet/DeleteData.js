export const DeleteData = ({ dataSetRepository }) => async dataSetId => dataSetRepository.deleteDataById(dataSetId);
