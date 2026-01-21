export class BusinessDataflow {
  constructor({
    bigData,
    creationDate,
    dataCollections,
    datasets,
    deleted,
    deletedAt,
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
    referenceDatasets,
    reportingDatasetsStatus,
    representatives,
    requestId,
    sncData,
    status,
    testDatasets,
    type,
    userRole,
    webLinks
  } = {}) {
    this.bigData = bigData;
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.deleted = deleted;
    this.deletedAt = deletedAt;
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
    this.referenceDatasets = referenceDatasets;
    this.reportingDatasetsStatus = reportingDatasetsStatus;
    this.representatives = representatives;
    this.requestId = requestId;
    this.sncData = sncData;
    this.status = status;
    this.testDatasets = testDatasets;
    this.type = type;
    this.userRole = userRole;
    this.webLinks = webLinks;
  }
}
