export class Feedback {
  constructor({ datetime, id, message, read, sender } = {}) {
    this.datetime = datetime;
    this.id = id;
    this.message = message;
    this.read = read;
    this.sender = sender;
  }
}
