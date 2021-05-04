export const UpdateReporter = ({ userRightRepository }) => async (userRight, dataflowId, dataProviderId) =>
  userRightRepository.updateReporter(userRight, dataflowId, dataProviderId);
