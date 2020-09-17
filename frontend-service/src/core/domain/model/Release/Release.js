export class Release {
  constructor({ countryCode, datasetId, datasetName, id, isDataCollectionReleased, isEUReleased, releasedData } = {}) {
    this.countryCode = countryCode;
    this.datasetId = datasetId;
    this.datasetName = datasetName;
    this.id = id;
    this.isDataCollectionReleased = isDataCollectionReleased;
    this.isEUReleased = isEUReleased;
    this.releasedData = releasedData;
  }
}
