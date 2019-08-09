export const GetDataSetSchema = ({ dataSetRepository }) => async dataFlowId =>
  dataSetRepository.dataSetSchemaById(dataFlowId);
