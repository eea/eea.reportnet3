export class Dataflow {
  constructor({
    creationDate,
    dataCollections,
    datasets,
    description,
    designDatasets,
    documents,
    euDatasets,
    expirationDate,
    id,
    isReleasable,
    manualAcceptance,
    name,
    obligation,
    reportingDatasetsStatus,
    representatives,
    requestId,
    showPublicInfo,
    status,
    userRequestStatus,
    userRole,
    weblinks
  } = {}) {
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.description = description;
    this.designDatasets = designDatasets;
    this.documents = documents;
    this.euDatasets = euDatasets;
    this.expirationDate = expirationDate;
    this.id = id;
    this.isReleasable = isReleasable;
    this.manualAcceptance = manualAcceptance;
    this.name = name;
    this.obligation = obligation;
    this.reportingDatasetsStatus = reportingDatasetsStatus;
    this.representatives = representatives;
    this.requestId = requestId;
    this.status = status;
    this.showPublicInfo = showPublicInfo;
    this.userRequestStatus = userRequestStatus;
    this.userRole = userRole;
    this.weblinks = weblinks;
  }
}
