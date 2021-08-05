export class Notification {
  constructor({
    content,
    date,
    downloadLink,
    fixed,
    id,
    key,
    lifeTime,
    message,
    onClick,
    read,
    redirectionUrl,
    type
  } = {}) {
    this.content = content;
    this.date = date;
    this.downloadLink = downloadLink;
    this.fixed = fixed;
    this.id = id;
    this.key = key;
    this.lifeTime = lifeTime;
    this.message = message;
    this.onClick = onClick;
    this.read = read;
    this.redirectionUrl = redirectionUrl;
    this.type = type;
  }
}
