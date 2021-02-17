export const GetPublicDataflowInformation = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.getPublicDataflowInformation(dataflowId);
