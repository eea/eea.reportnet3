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
    name,
    isValid = false
  ) {
    this.datasetId = datasetId;
    this.datasetSchemaDescription = datasetSchemaDescription;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.errors = errors;
    this.hasErrors = hasErrors;
    this.isReleased = isReleased;
    this.isValid = isValid;
    this.levelErrorTypes = levelErrorTypes;
    this.name = name;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
  }
}
