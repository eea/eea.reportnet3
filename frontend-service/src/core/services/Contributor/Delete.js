export const Delete = ({ contributorRepository }) => async (Contributor, dataflowId) =>
  contributorRepository.deleteContributor(Contributor, dataflowId);
