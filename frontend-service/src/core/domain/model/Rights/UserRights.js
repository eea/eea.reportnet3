export class UserRights {
  constructor({ account, dataProviderId, id = 0, isNew = false, role, writePermission } = {}) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.id = id;
    this.isNew = isNew;
    this.role = role;
    this.writePermission = writePermission;
  }
}
