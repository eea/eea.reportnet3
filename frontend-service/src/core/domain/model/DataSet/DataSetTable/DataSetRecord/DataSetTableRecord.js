export class DataSetTableRecord {
  constructor(dataSetPartitionId, recordId, recordSchemaId, fields, validations) {
    this.dataSetPartitionId = dataSetPartitionId;
    this.fields = fields;
    this.recordId = recordId;
    this.recordSchemaId = recordSchemaId;
    this.validations = validations;
  }
}
