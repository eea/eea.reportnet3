export class DatasetTableField {
  constructor({
    codelistId,
    description,
    fieldId,
    fieldSchemaId,
    name,
    recordId,
    required,
    type,
    validations,
    value
  } = {}) {
    this.codelistId = codelistId;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.name = name;
    this.recordId = recordId;
    this.required = required;
    this.type = type;
    this.validations = validations;
    this.value = value;
  }
}
