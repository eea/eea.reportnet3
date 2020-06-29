export const Add = ({ representativeRepository }) => async (dataflowId, providerAccount, dataProviderId) =>
  representativeRepository.add(dataflowId, providerAccount, dataProviderId);
