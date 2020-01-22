export const Delete = ({ codelistCategoryRepository }) => async codelistCategoryId =>
  codelistCategoryRepository.deleteById(codelistCategoryId);
