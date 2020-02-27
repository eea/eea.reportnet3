export class Representative {
  constructor(id, providerAccount, dataProviderId, dataProviderGroupId, isReceiptDownloaded, isReceiptOutdated) {
    this.dataProviderGroupId = dataProviderGroupId;
    this.dataProviderId = dataProviderId;
    this.isReceiptDownloaded = isReceiptDownloaded;
    this.isReceiptOutdated = isReceiptOutdated;
    this.providerAccount = providerAccount;
    this.representativeId = id;
  }
}
