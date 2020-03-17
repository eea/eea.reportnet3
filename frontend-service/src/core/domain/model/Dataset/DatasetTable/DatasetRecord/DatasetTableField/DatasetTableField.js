export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    pk,
    pkReferenced,
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
    this.pk = pk;
    this.pkReferenced = pkReferenced;
    this.name = name;
    this.recordId = recordId;
    this.required = required;
    this.type = type;
    this.validations = validations;
    this.value = value;
  }
}
