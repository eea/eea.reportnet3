export class Validation {
  constructor({ id, levelError, entityType, date, message } = {}) {
    this.date = date;
    this.entityType = entityType;
    this.id = id;
    this.levelError = levelError;
    this.message = message;
  }
}
