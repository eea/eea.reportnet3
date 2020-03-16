export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    isPK,
    isPKReferenced,
    name,
    recordId,
    required,
    type,
    validations,
    value
  } = {}) {
    this.codelistItems = codelistItems;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.isPK = isPK;
    this.isPKReferenced = isPKReferenced;
    this.name = name;
    this.recordId = recordId;
    this.required = required;
    this.type = type;
    this.validations = validations;
    this.value = value;
  }
}
