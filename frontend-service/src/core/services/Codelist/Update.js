export const Update = ({ codelistRepository }) => async (id, description, items, name, status, version, categoryId) =>
  codelistRepository.updateById(id, description, items, name, status, version, categoryId);
