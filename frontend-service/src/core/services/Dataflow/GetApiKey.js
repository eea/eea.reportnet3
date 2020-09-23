export const GetApiKey = ({ dataflowRepository }) => async (dataflowId, dataProviderId, isCustodian) =>
  dataflowRepository.getApiKey(dataflowId, dataProviderId, isCustodian);
