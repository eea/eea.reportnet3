export class Notification {
  constructor({ downloadLink, fixed, id, key, lifeTime, message, read, redirectionUrl, type } = {}) {
    this.downloadLink = downloadLink;
    this.fixed = fixed;
    this.id = id;
    this.key = key;
    this.lifeTime = lifeTime;
    this.message = message;
    this.read = read;
    this.redirectionUrl = redirectionUrl;
    this.type = type;
  }
}
