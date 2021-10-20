export class HistoricRelease {
  constructor({
    dataProviderCode,
    datasetId,
    datasetName,
    id,
    isDataCollectionReleased,
    isEUReleased,
    isRestrictedFromPublic,
    releaseDate
  } = {}) {
    this.dataProviderCode = dataProviderCode;
    this.datasetId = datasetId;
    this.datasetName = datasetName;
    this.id = id;
    this.isDataCollectionReleased = isDataCollectionReleased;
    this.isEUReleased = isEUReleased;
    this.isRestrictedFromPublic = isRestrictedFromPublic;
    this.releaseDate = releaseDate;
  }
}
