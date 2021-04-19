export const DeleteReporter = ({ rightsRepository }) => async (account, dataflowId, dataProviderId) =>
  rightsRepository.deleteReporter(account, dataflowId, dataProviderId);
