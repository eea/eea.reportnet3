export const GetSchema = ({ dataSetRepository }) => async dataFlowId => dataSetRepository.schemaById(dataFlowId);
