export const Update = ({ codelistRepository }) => async (dataflowId, codelistId, codelist) =>
  codelistRepository.updateById(dataflowId, codelistId, codelist);
