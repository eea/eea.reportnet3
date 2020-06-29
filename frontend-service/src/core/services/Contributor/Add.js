export const Add = ({ contributorRepository }) => async (Contributor, dataflowId) =>
  contributorRepository.add(Contributor, dataflowId);
