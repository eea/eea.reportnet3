export const UpdateWritePermission = ({ contributorRepository }) => async (Contributor, dataflowId) =>
  contributorRepository.updateWritePermission(Contributor, dataflowId);
