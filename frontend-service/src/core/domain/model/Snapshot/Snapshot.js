export class Snapshot {
  constructor(id, creationDate, description, isReleased) {
    this.creationDate = creationDate;
    this.description = description;
    this.id = id;
    this.isReleased = isReleased;
  }
}
