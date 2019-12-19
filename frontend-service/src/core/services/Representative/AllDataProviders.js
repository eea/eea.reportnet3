export const AllDataProviders = ({ representativeRepository }) => async groupId =>
  representativeRepository.allDataProviders(groupId);
