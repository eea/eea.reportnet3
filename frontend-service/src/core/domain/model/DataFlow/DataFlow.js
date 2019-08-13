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
    weblinks
  ) {
    this.id = id;
    this.datasets = datasets;
    this.description = description;
    this.name = name;
    this.deadlineDate = deadlineDate;
    this.creationDate = creationDate;
    this.userRequestStatus = userRequestStatus;
    this.status = status;
    this.documents = documents;
    this.weblinks = weblinks;
  }
}
