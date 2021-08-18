export class Dataflow {
  constructor({
    anySchemaAvailableInPublic,
    creationDate,
    dataCollections,
    dataProviderGroupId,
    dataProviderGroupName,
    datasets,
    description,
    designDatasets,
    documents,
    euDatasets,
    expirationDate,
    fmeUserId,
    fmeUserName,
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
    type,
    userRole,
    webLinks
  } = {}) {
    this.anySchemaAvailableInPublic = anySchemaAvailableInPublic;
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderGroupName = dataProviderGroupName;
    this.datasets = datasets;
    this.description = description;
    this.designDatasets = designDatasets;
    this.documents = documents;
    this.euDatasets = euDatasets;
    this.expirationDate = expirationDate;
    this.fmeUserId = fmeUserId;
    this.fmeUserName = fmeUserName;
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
    this.type = type;
    this.userRole = userRole;
    this.webLinks = webLinks;
  }
}
