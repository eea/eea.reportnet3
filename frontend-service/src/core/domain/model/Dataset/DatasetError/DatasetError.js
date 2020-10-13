export class DatasetError {
  constructor({
    entityType,
    levelError,
    message,
    numberOfRecords,
    objectId,
    position,
    recordId,
    ruleId,
    tableSchemaId,
    tableSchemaName,
    validationDate,
    validationId
  } = {}) {
    this.entityType = entityType;
    this.levelError = levelError;
    this.message = message;
    this.numberOfRecords = numberOfRecords;
    this.objectId = objectId;
    this.position = position;
    this.recordId = recordId;
    this.ruleId = ruleId;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.validationDate = validationDate;
    this.validationId = validationId;
  }
}
