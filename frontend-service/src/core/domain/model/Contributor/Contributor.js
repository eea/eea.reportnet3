export class Contributor {
  constructor({ account, dataProviderId, writePermission } = {}) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.writePermission = writePermission;
  }
}
