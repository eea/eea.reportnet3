export const DeleteEditor = ({ contributorRepository }) => async (account, dataflowId) =>
  contributorRepository.deleteEditor(account, dataflowId);
