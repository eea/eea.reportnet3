export const UpdateReporter = ({ userRightRepository }) => async (dataflowId, dataProviderId) =>
  userRightRepository.updateReporter(dataflowId, dataProviderId);
