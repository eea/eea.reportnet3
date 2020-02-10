export class Snapshot {
  constructor(id, creationDate, description, isReleased, isCreated, isDeleted, isRestored, isValid = false) {
    this.creationDate = creationDate;
    this.description = description;
    this.id = id;
    this.isCreated = isCreated;
    this.isDeleted = isDeleted;
    this.isReleased = isReleased;
    this.isRestored = isRestored;
    this.isValid = isValid;
  }
}
