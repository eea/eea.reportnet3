export const Delete = ({ contributorRepository }) => async (account, dataflowId, dataProviderId) =>
  contributorRepository.delete(account, dataflowId, dataProviderId);
