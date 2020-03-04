export class DatasetError {
  constructor({
    entityType,
    levelError,
    message,
    objectId,
    position,
    recordId,
    tableSchemaId,
    tableSchemaName,
    validationDate,
    validationId
  } = {}) {
    this.entityType = entityType;
    this.levelError = levelError;
    this.message = message;
    this.objectId = objectId;
    this.position = position;
    this.recordId = recordId;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.validationDate = validationDate;
    this.validationId = validationId;
  }
}
