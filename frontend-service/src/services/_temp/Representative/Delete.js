export const Delete = ({ representativeRepository }) => async (representativeId, dataflowId) =>
  representativeRepository.deleteById(representativeId, dataflowId);
