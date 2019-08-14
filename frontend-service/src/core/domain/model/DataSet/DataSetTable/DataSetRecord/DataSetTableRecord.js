export class DataSetTableRecord {
  constructor(recordId, recordSchemaId, fields, validations) {
    this.fields = fields;
    this.recordId = recordId;
    this.recordSchemaId = recordSchemaId;
    this.validations = validations;
  }
}
