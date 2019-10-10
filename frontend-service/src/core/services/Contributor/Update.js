export const Update = ({ contributorRepository }) => async (dataflowId, contributorId, contributorRole) =>
  contributorRepository.updateById(dataflowId, contributorId, contributorRole);
