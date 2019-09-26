export const GetSchema = ({ dataSetRepository }) => async dataflowId => dataSetRepository.schemaById(dataflowId);
