export const GetCodelistsListWithSchemas = ({ codelistRepository }) => async datasetsSchemas =>
  codelistRepository.getCodelistsListWithSchemas(datasetsSchemas);
