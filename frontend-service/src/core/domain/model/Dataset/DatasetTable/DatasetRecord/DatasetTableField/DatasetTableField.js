export class DatasetTableField {
  constructor(fieldId, recordId, name, type, value, validations, fieldSchemaId, description, codelistId) {
    this.codelistId = codelistId;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.name = name;
    this.recordId = recordId;
    this.type = type;
    this.value = value;
    this.validations = validations;
  }
}
