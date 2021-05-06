export const DeleteReporter = ({ userRightRepository }) => async (userRight, dataflowId, dataProviderId) =>
  userRightRepository.deleteReporter(userRight, dataflowId, dataProviderId);
