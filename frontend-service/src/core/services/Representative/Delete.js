export const Delete = ({ representativeRepository }) => async (dataflowId, representativeId) =>
  representativeRepository.delete(dataflowId, representativeId);
