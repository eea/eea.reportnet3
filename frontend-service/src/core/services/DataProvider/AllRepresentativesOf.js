export const AllRepresentativesOf = ({ dataProviderRepository }) => async type =>
  dataProviderRepository.allRepresentativesOf(type);
