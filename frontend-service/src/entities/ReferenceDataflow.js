export class ReferenceDataflow {
  constructor({
    creationDate,
    dataCollections,
    datasets,
    description,
    designDatasets,
    expirationDate,
    id,
    isReleasable,
    manualAcceptance,
    name,
    referenceDatasets,
    reportingDatasetsStatus,
    representatives,
    requestId,
    showPublicInfo,
    status,
    testDatasets,
    userRole
  } = {}) {
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.description = description;
    this.designDatasets = designDatasets;
    this.expirationDate = expirationDate;
    this.id = id;
    this.isReleasable = isReleasable;
    this.manualAcceptance = manualAcceptance;
    this.name = name;
    this.referenceDatasets = referenceDatasets;
    this.reportingDatasetsStatus = reportingDatasetsStatus;
    this.representatives = representatives;
    this.requestId = requestId;
    this.showPublicInfo = showPublicInfo;
    this.status = status;
    this.testDatasets = testDatasets;
    this.userRole = userRole;
  }
}
