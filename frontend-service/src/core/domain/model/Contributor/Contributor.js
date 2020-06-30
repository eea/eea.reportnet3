export class Contributor {
  constructor({ account, dataProviderId, role, writePermission, isNew = false } = {}) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.role = role;
    this.writePermission = writePermission;
    this.isNew = isNew;
  }
}
