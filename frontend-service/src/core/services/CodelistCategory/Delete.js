export const Delete = ({ codelistCategoryRepository }) => async categoryId =>
  codelistCategoryRepository.deleteById(categoryId);
