export const Update = ({ codelistCategoryRepository }) => async (dataflowId, categoryId, category) =>
  codelistCategoryRepository.updateById(dataflowId, categoryId, category);
