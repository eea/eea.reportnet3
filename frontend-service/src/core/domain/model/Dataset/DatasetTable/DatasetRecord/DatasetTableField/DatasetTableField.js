export class DatasetTableField {
  constructor({ codelistId, description, fieldId, fieldSchemaId, name, recordId, type, validations, value } = {}) {
    this.codelistId = codelistId;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.name = name;
    this.recordId = recordId;
    this.type = type;
    this.validations = validations;
    this.value = value;
  }
}
