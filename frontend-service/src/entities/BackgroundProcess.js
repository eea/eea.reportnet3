export class BackgroundProcess {
  constructor({
    dataflowId,
    dataflowName,
    datasetId,
    datasetName,
    id,
    priority,
    processFinishingDate,
    processStartingDate,
    queuedDate,
    status,
    user
  } = {}) {
    this.dataflowId = dataflowId;
    this.dataflowName = dataflowName;
    this.datasetId = datasetId;
    this.datasetName = datasetName;
    this.id = id;
    this.priority = priority;
    this.processFinishingDate = processFinishingDate;
    this.processStartingDate = processStartingDate;
    this.queuedDate = queuedDate;
    this.status = status;
    this.user = user;
  }
}
