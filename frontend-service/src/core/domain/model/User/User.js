export class User {
  constructor(id, name, accessRole, contextRoles, preferredUsername, tokenExpireTime) {
    this.id = id;
    this.name = name;
    this.accessRole = accessRole;
    this.contextRoles = contextRoles;
    this.preferredUsername = preferredUsername;
    this.tokenExpireTime = tokenExpireTime;
  }
}
