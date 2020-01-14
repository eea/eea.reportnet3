export const Update = ({ codelistCategoryRepository }) => async (id, shortCode, description) =>
  codelistCategoryRepository.updateById(id, shortCode, description);
