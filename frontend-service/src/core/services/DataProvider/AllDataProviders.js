export const AllDataProviders = ({ dataProviderRepository }) => async type =>
  dataProviderRepository.allDataProviders(type);
