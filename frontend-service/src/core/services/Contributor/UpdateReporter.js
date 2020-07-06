export const UpdateReporter = ({ contributorRepository }) => async (reporter, dataflowId, dataProviderId) =>
  contributorRepository.updateReporter(reporter, dataflowId, dataProviderId);
