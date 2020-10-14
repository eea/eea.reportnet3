export const GetData = ({ datasetRepository }) => async (
  datasetId,
  tableSchemaId,
  pageNum,
  pageSize,
  fields = undefined,
  levelError = null,
  ruleId = ''
) => datasetRepository.tableDataById(datasetId, tableSchemaId, pageNum, pageSize, fields, levelError, ruleId);
