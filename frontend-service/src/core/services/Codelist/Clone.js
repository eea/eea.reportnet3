export const Clone = ({ codelistRepository }) => async (codelistId, description, items, name, version, categoryId) =>
  codelistRepository.cloneById(codelistId, description, items, name, version, categoryId);
