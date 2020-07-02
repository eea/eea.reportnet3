export class Contributor {
  constructor({ account, dataProviderId, isNew = false, role, writePermission } = {}) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.isNew = isNew;
    this.role = role;
    this.writePermission = writePermission;
  }
}
