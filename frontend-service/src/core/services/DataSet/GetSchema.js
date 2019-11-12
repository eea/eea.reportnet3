export const GetSchema = ({ datasetRepository }) => async datasetId => datasetRepository.schemaById(datasetId);
