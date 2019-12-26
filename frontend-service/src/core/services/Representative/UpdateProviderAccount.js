export const UpdateProviderAccount = ({ representativeRepository }) => async (representativeId, providerAccount) =>
  representativeRepository.updateProviderAccount(representativeId, providerAccount);
