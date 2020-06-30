export class Contributor {
  constructor({ account, dataProviderId, role, writePermission } = {}) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.role = role;
    this.writePermission = writePermission;
  }
}
