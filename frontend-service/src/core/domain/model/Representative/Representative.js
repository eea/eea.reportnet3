export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    providerAccount,
    hasDatasets = false
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.providerAccount = providerAccount;
    this.representativeId = id;
    this.hasDatasets = hasDatasets;
  }
}
