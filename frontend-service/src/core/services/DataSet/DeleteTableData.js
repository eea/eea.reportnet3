export const DeleteTableData = ({ dataSetRepository }) => async (dataSetId, tableId) =>
  dataSetRepository.deleteTableDataById(dataSetId, tableId);
