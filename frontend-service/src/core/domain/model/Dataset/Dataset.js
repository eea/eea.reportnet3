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
    isReleased,
    levelErrorTypes,
    datasetSchemaDescription,
    name
  ) {
    this.datasetId = datasetId;
    this.datasetSchemaDescription = datasetSchemaDescription;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.name = name;
    this.errors = errors;
    this.isReleased = isReleased;
    this.levelErrorTypes = levelErrorTypes;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
    this.hasErrors = hasErrors;
  }
}
