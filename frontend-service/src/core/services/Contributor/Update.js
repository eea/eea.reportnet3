export const Update = ({ contributorRepository }) => async (contributor, dataflowId, dataProviderId) =>
  contributorRepository.update(contributor, dataflowId, dataProviderId);
