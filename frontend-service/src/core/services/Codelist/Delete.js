export const Delete = ({ codelistRepository }) => async (dataflowId, codelistId) =>
  codelistRepository.deleteById(dataflowId, codelistId);
