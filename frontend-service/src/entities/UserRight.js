export class UserRight {
  constructor({ account, id = 0, isNew = false, isValid = false, role = '' }) {
    this.account = account;
    this.id = id;
    this.isNew = isNew;
    this.isValid = isValid;
    this.role = role;
  }
}
