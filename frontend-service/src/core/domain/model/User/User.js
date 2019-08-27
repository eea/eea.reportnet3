export class User {
  constructor(id, name, roles, preferredUsername, tokenExpireTime) {
    this.id = id;
    this.name = name;
    this.roles = roles;
    this.preferredUsername = preferredUsername;
    this.tokenExpireTime = tokenExpireTime;
  }
}
