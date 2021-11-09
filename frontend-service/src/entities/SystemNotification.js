export class SystemNotification {
  constructor({ enabled, id, lifeTime, message, type } = {}) {
    this.enabled = enabled;
    this.id = id;
    this.lifeTime = lifeTime;
    this.message = message;
    this.type = type;
  }
}
