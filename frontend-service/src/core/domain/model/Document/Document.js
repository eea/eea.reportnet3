export class Document {
  constructor(id, title, description, category, language, url) {
    this.category = category;
    this.description = description;
    this.id = id;
    this.language = language;
    this.title = title;
    this.url = url;
  }
}
