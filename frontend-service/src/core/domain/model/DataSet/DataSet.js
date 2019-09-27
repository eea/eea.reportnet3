export class Dataset {
  constructor(
    errors,
    datasetId,
    datasetSchemaId,
    datasetSchemaName,
    totalErrors,
    tables,
    hasErrors,
    tableStatisticPercentages,
    tableStatisticValues
  ) {
    this.datasetId = datasetId;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.errors = errors;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.hasErrors = hasErrors;
  }
}
