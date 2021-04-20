export class UserRight {
  constructor({ account, dataProviderId, id = 0, isNew = false, role = '' }) {
    this.account = account;
    this.dataProviderId = dataProviderId;
    this.id = id;
    this.isNew = isNew;
    this.role = role;
  }
}
