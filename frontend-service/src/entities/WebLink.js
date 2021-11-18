export class WebLink {
  constructor({ description, id, isPublic, url } = {}) {
    this.description = description;
    this.id = id;
    this.isPublic = isPublic;
    this.url = url;
  }
}
