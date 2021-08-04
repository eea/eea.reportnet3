export const DeleteTableData = ({ datasetRepository }) => async (datasetId, tableId) =>
  datasetRepository.deleteTableDataById(datasetId, tableId);
