export class Validation {
  constructor({ date, entityType, id, levelError, message } = {}) {
    this.date = date;
    this.entityType = entityType;
    this.id = id;
    this.levelError = levelError;
    this.message = message;
  }
}
