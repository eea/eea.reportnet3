export class Dataset {
  constructor({
    datasetId,
    datasetSchemaDescription,
    datasetSchemaId,
    datasetSchemaName,
    errors,
    hasErrors,
    isReleased,
    levelErrorTypes,
    name,
    tables,
    tableStatisticPercentages,
    tableStatisticValues,
    totalErrors,
    totalFilteredErrors
  } = {}) {
    this.datasetId = datasetId;
    this.datasetSchemaDescription = datasetSchemaDescription;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.errors = errors;
    this.hasErrors = hasErrors;
    this.isReleased = isReleased;
    this.levelErrorTypes = levelErrorTypes;
    this.name = name;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
  }
}
