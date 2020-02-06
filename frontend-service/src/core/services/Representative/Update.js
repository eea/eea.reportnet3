export const Update = ({ representativeRepository }) => async (
  dataflowId,
  representativeId,
  providerAccount,
  dataProviderId
) => representativeRepository.update(dataflowId, representativeId, providerAccount, dataProviderId);
