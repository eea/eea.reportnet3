export const AllReporters = ({ userRightRepository }) => async (dataflowId, dataProviderId) =>
  userRightRepository.allReporters(dataflowId, dataProviderId);
