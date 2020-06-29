export const All = ({ contributorRepository }) => async (dataflowId, dataProviderId, userId) =>
  contributorRepository.all((dataflowId, dataProviderId, userId));
