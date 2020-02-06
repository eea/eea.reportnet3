export class Notification {
  constructor(notificationId, key, message, redirectionUrl, downloadLink, type, fixed, lifeTime, read) {
    this.id = notificationId;
    this.key = key;
    this.downloadLink = downloadLink;
    this.fixed = fixed;
    this.lifeTime = lifeTime;
    this.message = message;
    this.read = read;
    this.redirectionUrl = redirectionUrl;
    this.type = type;
  }
}
