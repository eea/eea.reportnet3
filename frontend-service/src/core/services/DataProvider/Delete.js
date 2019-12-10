export const Delete = ({ dataProviderRepository }) => async (dataflowId, dataProviderId) =>
  dataProviderRepository.delete(dataflowId, dataProviderId);
