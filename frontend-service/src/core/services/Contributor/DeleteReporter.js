export const DeleteReporter = ({ contributorRepository }) => async (account, dataflowId, dataProviderId) =>
  contributorRepository.deleteReporter(account, dataflowId, dataProviderId);
