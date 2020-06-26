export const UpdateAccount = ({ representativeRepository }) => async (representativeId, account) =>
  representativeRepository.updateAccount(representativeId, account);
