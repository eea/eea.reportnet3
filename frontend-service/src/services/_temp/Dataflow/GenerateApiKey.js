export const GenerateApiKey = ({ dataflowRepository }) => async (dataflowId, dataProviderId, isCustodian) =>
  dataflowRepository.generateApiKey(dataflowId, dataProviderId, isCustodian);
