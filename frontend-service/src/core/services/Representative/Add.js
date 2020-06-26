export const Add = ({ representativeRepository }) => async (dataflowId, account, dataProviderId) =>
  representativeRepository.add(dataflowId, account, dataProviderId);
