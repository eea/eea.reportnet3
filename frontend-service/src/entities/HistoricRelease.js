export class HistoricRelease {
  constructor({
    dataProviderCode,
    datasetId,
    datasetName,
    id,
    isDataCollectionReleased,
    isEUReleased,
    releaseDate,
    restrictFromPublic
  } = {}) {
    this.dataProviderCode = dataProviderCode;
    this.datasetId = datasetId;
    this.datasetName = datasetName;
    this.id = id;
    this.isDataCollectionReleased = isDataCollectionReleased;
    this.isEUReleased = isEUReleased;
    this.releaseDate = releaseDate;
    this.restrictFromPublic = restrictFromPublic;
  }
}
