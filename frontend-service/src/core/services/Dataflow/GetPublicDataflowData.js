export const GetPublicDataflowData = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.getPublicDataflowData(dataflowId);
