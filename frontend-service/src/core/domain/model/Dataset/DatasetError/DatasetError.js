export class DatasetError {
  constructor({
    entityType,
    fieldNameSchema,
    levelError,
    message,
    numberOfRecords,
    objectId,
    position,
    recordId,
    ruleId,
    shortCode,
    tableSchemaId,
    tableSchemaName,
    validationDate,
    validationId
  } = {}) {
    this.entityType = entityType;
    this.fieldNameSchema = fieldNameSchema;
    this.levelError = levelError;
    this.message = message;
    this.numberOfRecords = numberOfRecords;
    this.objectId = objectId;
    this.position = position;
    this.recordId = recordId;
    this.ruleId = ruleId;
    this.shortCode = shortCode;
    this.tableSchemaId = tableSchemaId;
    this.tableSchemaName = tableSchemaName;
    this.validationDate = validationDate;
    this.validationId = validationId;
  }
}
