export const AllReporters = ({ rightsRepository }) => async (dataflowId, dataProviderId) =>
  rightsRepository.allReporters(dataflowId, dataProviderId);
