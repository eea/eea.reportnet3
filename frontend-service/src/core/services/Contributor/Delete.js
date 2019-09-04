export const Delete = ({ contributorRepository }) => async (dataFlowId, contributorId) =>
  contributorRepository.deleteById(dataFlowId, contributorId);
