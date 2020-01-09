export const Add = ({ codelistRepository }) => async (dataflowId, description, items, name, status, version) =>
  codelistRepository.addById(dataflowId, description, items, name, status, version);
