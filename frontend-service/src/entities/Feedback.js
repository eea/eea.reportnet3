export class Feedback {
  constructor({ content, date, direction, id, messageAttachment, providerId, read, type } = {}) {
    this.content = content;
    this.date = date;
    this.direction = direction;
    this.id = id;
    this.messageAttachment = messageAttachment;
    this.providerId = providerId;
    this.read = read;
    this.type = type;
  }
}
