export class EUDataset {
  constructor({ creationDate, euDatasetId, euDatasetName, dataflowId, datasetSchemaId, expirationDate, status } = {}) {
    this.creationDate = creationDate;
    this.euDatasetId = euDatasetId;
    this.euDatasetName = euDatasetName;
    this.dataflowId = dataflowId;
    this.datasetSchemaId = datasetSchemaId;
    this.expirationDate = expirationDate;
    this.status = status;
  }
}
