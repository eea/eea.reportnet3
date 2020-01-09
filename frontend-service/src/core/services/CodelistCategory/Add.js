export const Add = ({ codelistCategoryRepository }) => async (dataflowId, name, description, codelists) =>
  codelistCategoryRepository.addById(dataflowId, name, description, codelists);
