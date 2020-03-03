export class Validation {
  constructor({
    activationGroup,
    automatic,
    condition,
    date,
    description,
    enabled,
    entityType,
    id,
    levelError,
    message,
    name,
    referenceId,
    shortCode
  } = {}) {
    this.activationGroup = activationGroup;
    this.automatic = automatic;
    this.condition = condition;
    this.date = date;
    this.description = description;
    this.enabled = enabled;
    this.entityType = entityType;
    this.id = id;
    this.levelError = levelError;
    this.message = message;
    this.name = name;
    this.referenceId = referenceId;
    this.shortCode = shortCode;
  }
}
