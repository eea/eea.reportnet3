export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    hasDatasets,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    isReleasing,
    providerAccount
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.hasDatasets = hasDatasets;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.isReleasing = isReleasing;
    this.providerAccount = providerAccount;
    this.representativeId = id;
  }
}
