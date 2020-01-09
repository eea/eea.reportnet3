export const GetAll = ({ codelistCategoryRepository }) => async dataflowId =>
  codelistCategoryRepository.all(dataflowId);
