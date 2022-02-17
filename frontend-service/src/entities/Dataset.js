export class Dataset {
  constructor({
    availableInPublic,
    dataProviderId,
    datasetFeedbackStatus,
    datasetId,
    datasetRunningStatus,
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
    referenceDataset,
    releaseDate,
    restrictFromPublic,
    tables,
    tableStatisticPercentages,
    tableStatisticValues,
    status,
    totalErrors,
    totalFilteredErrors,
    totalRecords,
    updatable,
    webform
  } = {}) {
    this.availableInPublic = availableInPublic;
    this.dataProviderId = dataProviderId;
    this.datasetFeedbackStatus = datasetFeedbackStatus;
    this.datasetId = datasetId;
    this.datasetRunningStatus = datasetRunningStatus;
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
    this.referenceDataset = referenceDataset;
    this.releaseDate = releaseDate;
    this.restrictFromPublic = restrictFromPublic;
    this.tables = tables;
    this.tableStatisticPercentages = tableStatisticPercentages;
    this.tableStatisticValues = tableStatisticValues;
    this.status = status;
    this.totalErrors = totalErrors;
    this.totalFilteredErrors = totalFilteredErrors;
    this.totalRecords = totalRecords;
    this.updatable = updatable;
    this.webform = webform;
  }
}
