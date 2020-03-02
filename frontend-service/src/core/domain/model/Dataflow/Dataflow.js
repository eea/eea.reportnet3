export class Dataflow {
  constructor(
    id,
    dataCollections,
    datasets,
    designDatasets,
    description,
    name,
    deadlineDate,
    creationDate,
    userRequestStatus,
    status,
    documents,
    weblinks,
    requestId,
    representatives
  ) {
    this.creationDate = creationDate;
    this.dataCollections = dataCollections;
    this.datasets = datasets;
    this.deadlineDate = deadlineDate;
    this.description = description;
    this.designDatasets = designDatasets;
    this.documents = documents;
    this.id = id;
    this.name = name;
    this.representatives = representatives;
    this.requestId = requestId;
    this.status = status;
    this.userRequestStatus = userRequestStatus;
    this.weblinks = weblinks;
  }
}
