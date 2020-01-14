export const Add = ({ codelistCategoryRepository }) => async (shortCode, description) =>
  codelistCategoryRepository.addById(shortCode, description);
