export class ReferencedDataflow {
  constructor({
    creationDate,
    dataCollections,
    datasets,
    description,
    designDatasets,
    euDatasets,
    expirationDate,
    id,
    isReleasable,
    manualAcceptance,
    name,
    reportingDatasetsStatus,
    representatives,
    requestId,
    showPublicInfo,
    status,
    userRole
  } = {}) {
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.description = description;
    this.designDatasets = designDatasets;
    this.euDatasets = euDatasets;
    this.expirationDate = expirationDate;
    this.id = id;
    this.isReleasable = isReleasable;
    this.manualAcceptance = manualAcceptance;
    this.name = name;
    this.reportingDatasetsStatus = reportingDatasetsStatus;
    this.representatives = representatives;
    this.requestId = requestId;
    this.showPublicInfo = showPublicInfo;
    this.status = status;
    this.userRole = userRole;
  }
}
