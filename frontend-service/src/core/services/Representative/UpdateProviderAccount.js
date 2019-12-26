export const UpdateProviderAccount = ({ representativeRepository }) => async (representativeId, providerAccount) =>
  representativeRepository.UpdateProviderAccount(representativeId, providerAccount);
