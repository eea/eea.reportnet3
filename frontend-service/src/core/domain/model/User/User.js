export class User {
  constructor(id, name, mainRole, contextRoles, preferredUsername, tokenExpireTime) {
    this.id = id;
    this.name = name;
    this.mainRole = mainRole;
    this.contextRoles = contextRoles;
    this.preferredUsername = preferredUsername;
    this.tokenExpireTime = tokenExpireTime;
  }
}
