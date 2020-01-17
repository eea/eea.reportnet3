export const GetCodelistsList = ({ codelistRepository }) => async datasetSchemas =>
  codelistRepository.getCodelistsList(datasetSchemas);
