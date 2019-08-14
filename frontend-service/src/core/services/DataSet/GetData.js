export const GetData = ({ dataSetRepository }) => async (
  dataSetId,
  tableSchemaId,
  pageNum,
  pageSize,
  fields = undefined
) => dataSetRepository.tableDataById(dataSetId, tableSchemaId, pageNum, pageSize, fields);
