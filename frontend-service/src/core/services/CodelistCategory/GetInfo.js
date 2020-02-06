export const GetInfo = ({ codelistCategoryRepository }) => async codelistCategoryId =>
  codelistCategoryRepository.getCategoryInfo(codelistCategoryId);
