export const GetData = ({ datasetRepository }) => async ({
  datasetId,
  fields = undefined,
  fieldSchemaId = undefined,
  levelError = null,
  pageNum,
  pageSize,
  ruleId = undefined,
  tableSchemaId,
  value = ''
}) =>
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
