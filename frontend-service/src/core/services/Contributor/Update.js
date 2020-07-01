export const Update = ({ contributorRepository }) => async (reporter, dataflowId, dataProviderId) =>
  contributorRepository.updateReporter(reporter, dataflowId, dataProviderId);
