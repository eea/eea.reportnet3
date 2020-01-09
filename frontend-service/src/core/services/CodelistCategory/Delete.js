export const Delete = ({ codelistCategoryRepository }) => async (dataflowId, categoryId) =>
  codelistCategoryRepository.deleteById(dataflowId, categoryId);
