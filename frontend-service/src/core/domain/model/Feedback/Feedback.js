export class Feedback {
  constructor({ content, date, id, providerId, read, direction } = {}) {
    this.content = content;
    this.date = date;
    this.id = id;
    this.providerId = providerId;
    this.read = read;
    this.direction = direction;
  }
}
