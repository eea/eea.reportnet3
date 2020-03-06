export class User {
  constructor({ accessRole, contextRoles, id, name, preferredUsername, tokenExpireTime } = {}) {
    this.accessRole = accessRole;
    this.contextRoles = contextRoles;
    this.id = id;
    this.name = name;
    this.preferredUsername = preferredUsername;
    this.tokenExpireTime = tokenExpireTime;
  }
}
