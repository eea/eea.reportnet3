export const Add = ({ contributorRepository }) => async (dataFlowId, contributorLogin, contributorRole) =>
  contributorRepository.addByLogin(dataFlowId, contributorLogin, contributorRole);
