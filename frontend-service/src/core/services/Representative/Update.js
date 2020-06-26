export const Update = ({ representativeRepository }) => async (dataflowId, representativeId, account, dataProviderId) =>
  representativeRepository.update(dataflowId, representativeId, account, dataProviderId);
