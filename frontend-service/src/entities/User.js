export class User {
  constructor({
    accessRole,
    contextRoles,
    email,
    firstName,
    id,
    lastName,
    name,
    preferredUsername,
    tokenExpireTime
  } = {}) {
    this.accessRole = accessRole;
    this.contextRoles = contextRoles;
    this.email = email;
    this.firstName = firstName;
    this.id = id;
    this.lastName = lastName;
    this.name = name;
    this.preferredUsername = preferredUsername;
    this.tokenExpireTime = tokenExpireTime;
  }
}
