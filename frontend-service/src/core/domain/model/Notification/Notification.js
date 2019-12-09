export class Notification {
  constructor(notificationId, message, redirectionUrl, downloadLink, type, fixed, lifeTime, readed) {
    this.id = notificationId;
    this.message = message;
    this.redirectionUrl = redirectionUrl;
    this.downloadLink = downloadLink;
    this.type = type;
    this.fixed = fixed;
    this.lifeTime = lifeTime;
    this.readed = readed;
  }
}
