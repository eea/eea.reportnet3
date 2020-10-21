export class Feedback {
  constructor({
    datetime,
    id,
    message,
    read
  } = {}) {
    this.datetime = datetime;
    this.id = id;
    this.message = message;
    this.read = read;
  }
}
