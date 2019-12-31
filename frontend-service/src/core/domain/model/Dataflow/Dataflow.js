export class Dataflow {
  constructor(
    id,
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
    requestId
  ) {
    this.creationDate = creationDate;
    this.datasets = datasets;
    this.designDatasets = designDatasets;
    this.deadlineDate = deadlineDate;
    this.description = description;
    this.documents = documents;
    this.id = id;
    this.name = name;
    this.status = status;
    this.userRequestStatus = userRequestStatus;
    this.weblinks = weblinks;
    this.requestId = requestId;
  }
}
