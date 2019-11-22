export class Document {
  constructor(id, title, description, category, language, url, isPublic, date, size) {
    this.category = category;
    this.description = description;
    this.id = id;
    this.language = language;
    this.title = title;
    this.url = url;
    this.isPublic = isPublic;
    this.date = date;
    this.size = size;
  }
}
