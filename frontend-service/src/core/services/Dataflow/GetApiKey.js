export const GetApiKey = ({ dataflowRepository }) => async (dataflowId, dataProviderId) =>
  dataflowRepository.getApiKey(dataflowId, dataProviderId);
