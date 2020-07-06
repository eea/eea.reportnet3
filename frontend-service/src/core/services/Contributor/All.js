export const All = ({ contributorRepository }) => async (dataflowId, dataProviderId) =>
  contributorRepository.all(dataflowId, dataProviderId);
