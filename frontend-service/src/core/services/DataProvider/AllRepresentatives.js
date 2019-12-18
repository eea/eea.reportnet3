export const AllRepresentatives = ({ dataProviderRepository }) => async dataflowId =>
  dataProviderRepository.all(dataflowId);
