export class Dataflow {
  constructor({
    creationDate,
    dataCollections,
    datasets,
    description,
    designDatasets,
    documents,
    expirationDate,
    id,
    name,
    representatives,
    requestId,
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
    this.expirationDate = expirationDate;
    this.id = id;
    this.name = name;
    this.representatives = representatives;
    this.requestId = requestId;
    this.status = status;
    this.userRequestStatus = userRequestStatus;
    this.userRole = userRole;
    this.weblinks = weblinks;
  }
}
