export const GetData = ({ datasetRepository }) => async (
  datasetId,
  tableSchemaId,
  pageNum,
  pageSize,
  fields = undefined,
  levelError = null,
  ruleId = undefined,
  fieldSchemaId = undefined,
  value = undefined
) =>
  datasetRepository.tableDataById(
    datasetId,
    tableSchemaId,
    pageNum,
    pageSize,
    fields,
    levelError,
    ruleId,
    fieldSchemaId,
    value
  );
