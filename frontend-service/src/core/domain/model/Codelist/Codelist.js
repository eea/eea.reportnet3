export class Codelist {
  constructor({ description, id, items, name, status, version } = {}) {
    this.description = description;
    this.id = id;
    this.items = items;
    this.name = name;
    this.status = status;
    this.version = version;
  }
}
