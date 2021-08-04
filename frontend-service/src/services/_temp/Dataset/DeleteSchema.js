export const DeleteSchema = ({ datasetRepository }) => async datasetId => datasetRepository.deleteSchemaById(datasetId);
