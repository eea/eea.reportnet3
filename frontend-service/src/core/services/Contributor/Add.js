export const Add = ({ contributorRepository }) => async (dataflowId, contributorLogin, contributorRole) =>
  contributorRepository.addByLogin(dataflowId, contributorLogin, contributorRole);
