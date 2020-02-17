export class Representative {
  constructor({ dataProviderId, id, providerAccount } = {}) {
    this.dataProviderId = dataProviderId;
    this.providerAccount = providerAccount;
    this.representativeId = id;
  }
}
