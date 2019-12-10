export const Update = ({ dataProviderRepository }) => async (
  dataflowId,
  dataProviderId,
  dataProviderEmail,
  dataProviderName
) => dataProviderRepository.update(dataflowId, dataProviderId, dataProviderEmail, dataProviderName);
