export const UpdateWritePermission = ({ contributorRepository }) => async (contributor, dataflowId) =>
  contributorRepository.updateWritePermission(contributor, dataflowId);
