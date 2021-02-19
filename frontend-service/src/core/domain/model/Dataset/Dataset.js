export class Dataset {
  constructor({
    availableInPublic,
    dataProviderId,
    datasetFeedbackStatus,
    datasetId,
    datasetSchemaDescription,
    datasetSchemaId,
    datasetSchemaName,
    errors,
    hasErrors,
    isReleased,
    isReleasing,
    isValid = false,
    levelErrorTypes,
    name,
    publicFileName,
    releaseDate,
    tables,
    tableStatisticPercentages,
    tableStatisticValues,
    totalErrors,
    totalFilteredErrors,
    totalRecords,
    webform
  } = {}) {
    this.availableInPublic = availableInPublic;
    this.dataProviderId = dataProviderId;
    this.datasetFeedbackStatus = datasetFeedbackStatus;
    this.datasetId = datasetId;
    this.datasetSchemaDescription = datasetSchemaDescription;
    this.datasetSchemaId = datasetSchemaId;
    this.datasetSchemaName = datasetSchemaName;
    this.errors = errors;
    this.hasErrors = hasErrors;
    this.isReleased = isReleased;
    this.isReleasing = isReleasing;
    this.isValid = isValid;
    this.levelErrorTypes = levelErrorTypes;
    this.name = name;
    this.publicFileName = publicFileName;
    this.releaseDate = releaseDate;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
    this.totalRecords = totalRecords;
    this.webform = webform;
  }
}
