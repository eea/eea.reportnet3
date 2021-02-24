export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    hasDatasets,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    leadReporters
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.hasDatasets = hasDatasets;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.leadReporters = leadReporters;
    this.representativeId = id;
  }
}
