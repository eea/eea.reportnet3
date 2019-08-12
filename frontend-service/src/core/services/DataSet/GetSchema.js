export const GetSchema = ({ dataSetRepository }) => async dataFlowId => dataSetRepository.dataSetSchemaById(dataFlowId);
