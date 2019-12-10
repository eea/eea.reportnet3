export const Add = ({ dataProviderRepository }) => async (dataflowId, dataProviderEmail, dataProvider) =>
  dataProviderRepository.add(dataflowId, dataProviderEmail, dataProvider);
