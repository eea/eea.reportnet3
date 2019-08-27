export const Update = ({ contributorRepository }) => async (dataFlowId, contributorId, contributorRole) =>
  contributorRepository.updateById(dataFlowId, contributorId, contributorRole);
