export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    hasDatasets,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    providerAccount
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.hasDatasets = hasDatasets;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.providerAccount = providerAccount;
    this.representativeId = id;
  }
}
