export const AllReporters = ({ contributorRepository }) => async (dataflowId, dataProviderId) =>
  contributorRepository.allReporters(dataflowId, dataProviderId);
