export class DataSetTableField {
  constructor(fieldId, recordId, name, type, value, validations) {
    this.fieldId = fieldId;
    this.name = name;
    this.recordId = recordId;
    this.type = type;
    this.value = value;
    this.validations = validations;
  }
}
