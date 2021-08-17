export class DataCollection {
  constructor({
    creationDate,
    dataCollectionId,
    dataCollectionName,
    dataflowId,
    datasetSchemaId,
    expirationDate,
    status
  } = {}) {
    this.creationDate = creationDate;
    this.dataCollectionId = dataCollectionId;
    this.dataCollectionName = dataCollectionName;
    this.dataflowId = dataflowId;
    this.datasetSchemaId = datasetSchemaId;
    this.expirationDate = expirationDate;
    this.status = status;
  }
}
