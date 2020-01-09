export class CodelistCategory {
  constructor(id, name, description, codelists) {
    this.codelists = codelists;
    this.description = description;
    this.id = id;
    this.name = name;
  }
}
