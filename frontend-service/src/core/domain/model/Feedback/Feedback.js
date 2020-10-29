export class Feedback {
  constructor({ datetime, id, content, read, sender } = {}) {
    this.datetime = datetime;
    this.id = id;
    this.content = content;
    this.read = read;
    this.sender = sender;
  }
}
