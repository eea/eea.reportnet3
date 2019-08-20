export const ExportTableData = ({ dataSetRepository }) => async (dataSetId, tableSchemaId) =>
  dataSetRepository.exportTableDataById(dataSetId, tableSchemaId);
