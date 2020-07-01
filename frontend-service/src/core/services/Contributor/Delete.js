export const DeleteContributor = ({ contributorRepository }) => async (account, dataflowId, dataProviderId) =>
  contributorRepository.delete(account, dataflowId, dataProviderId);
