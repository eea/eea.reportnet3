export class Feedback {
  constructor({ attachment, content, date, id, providerId, read, direction } = {}) {
    this.attachment = attachment;
    this.content = content;
    this.date = date;
    this.id = id;
    this.providerId = providerId;
    this.read = read;
    this.direction = direction;
  }
}
