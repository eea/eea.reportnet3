export class Codelist {
  constructor(id, name, description, version, status, items) {
    this.description = description;
    this.id = id;
    this.items = items;
    this.name = name;
    this.status = status;
    this.version = version;
  }
}
