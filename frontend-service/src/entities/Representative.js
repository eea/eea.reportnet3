export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    hasDatasets,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    leadReporters,
    restrictFromPublic
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.hasDatasets = hasDatasets;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.leadReporters = leadReporters;
    this.representativeId = id;
    this.restrictFromPublic = restrictFromPublic;
  }
}
