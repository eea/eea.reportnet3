export class Contributor {
  constructor({ id, login, role } = {}) {
    this.id = id;
    this.login = login;
    this.role = role;
  }
}
