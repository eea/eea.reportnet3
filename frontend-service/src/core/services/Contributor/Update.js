export const Update = ({ contributorRepository }) => async (dataFlowId, contributorId, contributorRole) =>
  contributorRepository.releaseById(dataFlowId, contributorId, contributorRole);
