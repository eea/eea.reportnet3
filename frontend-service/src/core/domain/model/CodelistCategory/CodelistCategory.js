export class CodelistCategory {
  constructor(id, shortCode, description, codelists) {
    this.codelists = codelists;
    this.description = description;
    this.id = id;
    this.shortCode = shortCode;
  }
}
