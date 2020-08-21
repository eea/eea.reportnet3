export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    errorMessages = new Set(),
    hasDatasets,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    providerAccount
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.errorMessages = errorMessages;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.providerAccount = providerAccount;
    this.representativeId = id;
    this.hasDatasets = hasDatasets;
  }
}
