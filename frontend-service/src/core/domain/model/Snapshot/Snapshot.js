export class Snapshot {
  constructor({ creationDate, description, id, isBlocked = false, isCreated, isDeleted, isReleased, isRestored } = {}) {
    this.creationDate = creationDate;
    this.description = description;
    this.id = id;
    this.isBlocked = isBlocked;
    this.isCreated = isCreated;
    this.isDeleted = isDeleted;
    this.isReleased = isReleased;
    this.isRestored = isRestored;
  }
}
