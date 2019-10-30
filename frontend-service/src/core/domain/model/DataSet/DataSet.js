export class Dataset {
  constructor(
    errors,
    datasetId,
    datasetSchemaId,
    datasetSchemaName,
    totalErrors,
    totalFilteredErrors,
    tables,
    hasErrors,
    tableStatisticPercentages,
    tableStatisticValues,
    isReleased
  ) {
    this.datasetId = datasetId;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.errors = errors;
    this.isReleased = isReleased;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
    this.hasErrors = hasErrors;
  }
}
