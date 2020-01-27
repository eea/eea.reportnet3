export const GetAllInCategory = ({ codelistRepository }) => async codelistCategoryId =>
  codelistRepository.allInCategory(codelistCategoryId);
