export class Dataflow {
  constructor({
    anySchemaAvailableInPublic,
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
    referenceDatasets,
    reportingDatasetsStatus,
    representatives,
    requestId,
    showPublicInfo,
    status,
    testDatasets,
    userRole,
    weblinks
  } = {}) {
    this.anySchemaAvailableInPublic = anySchemaAvailableInPublic;
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
    this.referenceDatasets = referenceDatasets;
    this.reportingDatasetsStatus = reportingDatasetsStatus;
    this.representatives = representatives;
    this.requestId = requestId;
    this.showPublicInfo = showPublicInfo;
    this.status = status;
    this.testDatasets = testDatasets;
    this.userRole = userRole;
    this.weblinks = weblinks;
  }
}
