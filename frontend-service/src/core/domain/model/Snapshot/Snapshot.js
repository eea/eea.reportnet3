export class Snapshot {
  constructor({ creationDate, description, id, isAutomatic, isCreated, isDeleted, isReleased, isRestored } = {}) {
    this.creationDate = creationDate;
    this.description = description;
    this.id = id;
    this.isAutomatic = isAutomatic;
    this.isCreated = isCreated;
    this.isDeleted = isDeleted;
    this.isReleased = isReleased;
    this.isRestored = isRestored;
  }
}
