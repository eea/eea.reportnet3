export class Representative {
  constructor({
    dataProviderGroupId,
    dataProviderId,
    id,
    isReceiptDownloaded,
    isReceiptOutdated,
    permission = true,
    providerAccount,
    hasDatasets
  } = {}) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.providerAccount = providerAccount;
    this.permission = permission;
    this.representativeId = id;
    this.hasDatasets = hasDatasets;
  }
}
