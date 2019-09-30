export const Delete = ({ contributorRepository }) => async (dataflowId, contributorId) =>
  contributorRepository.deleteById(dataflowId, contributorId);
