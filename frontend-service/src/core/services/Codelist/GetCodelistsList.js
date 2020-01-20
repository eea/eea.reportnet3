export const GetCodelistsList = ({ codelistRepository }) => async datasetsSchemas =>
  codelistRepository.getCodelistsList(datasetsSchemas);
