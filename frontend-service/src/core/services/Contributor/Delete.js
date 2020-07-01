export const Delete = ({ contributorRepository }) => async (contributor, dataflowId) =>
  contributorRepository.deleteContributor(contributor, dataflowId);
