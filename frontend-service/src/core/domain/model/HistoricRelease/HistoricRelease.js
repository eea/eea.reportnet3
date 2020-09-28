export class HistoricRelease {
  constructor({ countryCode, datasetId, datasetName, id, isDataCollectionReleased, isEUReleased, releasedDate } = {}) {
    this.countryCode = countryCode;
    this.datasetId = datasetId;
    this.datasetName = datasetName;
    this.id = id;
    this.isDataCollectionReleased = isDataCollectionReleased;
    this.isEUReleased = isEUReleased;
    this.releasedDate = releasedDate;
  }
}
