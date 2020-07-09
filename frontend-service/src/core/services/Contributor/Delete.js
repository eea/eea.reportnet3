export const Delete = ({ contributorRepository }) => async (account, dataflowId, dataProviderId) =>
  contributorRepository.deleteContributor(account, dataflowId, dataProviderId);
