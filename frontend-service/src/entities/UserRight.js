export class UserRight {
  constructor({ account, id = 0, isNew = false, role = '' }) {
    this.account = account;
    this.id = id;
    this.isNew = isNew;
    this.role = role;
  }
}
