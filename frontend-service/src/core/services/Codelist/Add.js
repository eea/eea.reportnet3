export const Add = ({ codelistRepository }) => async (description, items, name, status, version, categoryId) =>
  codelistRepository.addById(description, items, name, status, version, categoryId);
