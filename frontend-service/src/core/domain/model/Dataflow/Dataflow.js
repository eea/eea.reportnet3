export class Dataflow {
  constructor({
    creationDate,
    dataCollections,
    datasets,
    deadlineDate,
    description,
    designDatasets,
    documents,
    id,
    name,
    requestId,
    status,
    userRequestStatus,
    weblinks
  } = {}) {
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.designDatasets = designDatasets;
    this.deadlineDate = deadlineDate;
    this.description = description;
    this.documents = documents;
    this.id = id;
    this.name = name;
    this.requestId = requestId;
    this.status = status;
    this.userRequestStatus = userRequestStatus;
    this.weblinks = weblinks;
  }
}
