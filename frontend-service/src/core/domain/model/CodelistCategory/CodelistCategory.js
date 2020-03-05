export class CodelistCategory {
  constructor({ codelistNumber, codelists, description, id, shortCode } = {}) {
    this.codelistNumber = codelistNumber;
    this.codelists = codelists;
    this.description = description;
    this.id = id;
    this.shortCode = shortCode;
  }
}
