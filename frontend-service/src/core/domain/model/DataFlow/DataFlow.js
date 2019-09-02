export class DataFlow {
  constructor(
    id,
    datasets,
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
    this.deadlineDate = deadlineDate;
    this.description = description;
    this.documents = documents;
    this.id = id;
    this.name = name;
    this.status = status;
    this.userRequestStatus = userRequestStatus;
    this.weblinks = weblinks;
    this.requestId  = requestId;
  }
}
