export const GetDetails = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.dataflowDetails(dataflowId);
