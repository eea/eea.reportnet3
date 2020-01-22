export class DataCollection {
  constructor(dataCollectionId, dataCollectionName, dataflowId, datasetSchemaId, creationDate, expirationDate, status) {
    this.dataCollectionId = dataCollectionId;
    this.dataCollectionName = dataCollectionName;
    this.dataflowId = dataflowId;
    this.datasetSchemaId = datasetSchemaId;
    this.creationDate = creationDate;
    this.expirationDate = expirationDate;
    this.status = status;
  }
}
