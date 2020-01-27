export class CodelistCategory {
  constructor(id, shortCode, description, codelists, codelistNumber) {
    this.codelistNumber = codelistNumber;
    this.codelists = codelists;
    this.description = description;
    this.id = id;
    this.shortCode = shortCode;
  }
}
