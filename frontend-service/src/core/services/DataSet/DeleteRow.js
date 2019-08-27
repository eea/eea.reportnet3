export const DeleteRow = ({ dataSetRepository }) => async (dataSetId, rowIds) =>
  dataSetRepository.deleteRowByIds(dataSetId, rowIds);
