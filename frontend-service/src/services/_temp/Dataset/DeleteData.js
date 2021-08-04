export const DeleteData = ({ datasetRepository }) => async datasetId => datasetRepository.deleteDataById(datasetId);
