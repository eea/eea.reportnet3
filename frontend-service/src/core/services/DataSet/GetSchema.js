export const GetSchema = ({ datasetRepository }) => async dataflowId => datasetRepository.schemaById(dataflowId);
