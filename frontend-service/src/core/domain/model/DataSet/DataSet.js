export class DataSet {
  constructor(
    errors,
    dataSetId,
    dataSetSchemaId,
    dataSetSchemaName,
    totalErrors,
    tables,
    hasErrors,
    tableStatisticPercentages,
    tableStatisticValues
  ) {
    this.dataSetId = dataSetId;
    this.dataSetSchemaId = dataSetSchemaId;
    this.dataSetSchemaName = dataSetSchemaName;
    this.errors = errors;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.hasErrors = hasErrors;
  }
}
