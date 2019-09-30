export const GetData = ({ datasetRepository }) => async (
  datasetId,
  tableSchemaId,
  pageNum,
  pageSize,
  fields = undefined
) => datasetRepository.tableDataById(datasetId, tableSchemaId, pageNum, pageSize, fields);
