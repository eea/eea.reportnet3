export class SystemNotification {
  constructor({ enabled, id, lifeTime, message, level } = {}) {
    this.enabled = enabled;
    this.id = id;
    this.lifeTime = lifeTime;
    this.message = message;
    this.level = level;
  }
}
