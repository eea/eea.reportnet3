export const ExportTableData = ({ dataSetRepository }) => async (dataSetId, tableSchemaId, fileType) =>
  dataSetRepository.exportTableDataById(dataSetId, tableSchemaId, fileType);
