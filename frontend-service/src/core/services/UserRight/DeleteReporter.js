export const DeleteReporter = ({ userRightRepository }) => async (dataflowId, dataProviderId) =>
  userRightRepository.deleteReporter(dataflowId, dataProviderId);
