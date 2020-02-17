export class Snapshot {
  constructor({ creationDate, description, id, isCreated, isDeleted, isReleased, isRestored } = {}) {
    this.creationDate = creationDate;
    this.description = description;
    this.id = id;
    this.isCreated = isCreated;
    this.isDeleted = isDeleted;
    this.isReleased = isReleased;
    this.isRestored = isRestored;
  }
}
