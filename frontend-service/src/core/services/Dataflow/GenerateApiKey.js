export const GenerateApiKey = ({ dataflowRepository }) => async (dataflowId, dataProviderId) =>
  dataflowRepository.generateApiKey(dataflowId, dataProviderId);
