export class Document {
  constructor({ category, date, description, id, isPublic, language, size, title } = {}) {
    this.category = category;
    this.date = date;
    this.description = description;
    this.id = id;
    this.isPublic = isPublic;
    this.language = language;
    this.size = size;
    this.title = title;
  }
}
