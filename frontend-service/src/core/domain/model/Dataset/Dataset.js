export class Dataset {
  constructor({
    dataProviderId,
    datasetId,
    datasetSchemaDescription,
    datasetSchemaId,
    datasetSchemaName,
    errors,
    hasErrors,
    isReleased,
    isValid = false,
    levelErrorTypes,
    name,
    tables,
    tableStatisticPercentages,
    tableStatisticValues,
    totalErrors,
    totalFilteredErrors
  } = {}) {
    this.dataProviderId = dataProviderId;
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
