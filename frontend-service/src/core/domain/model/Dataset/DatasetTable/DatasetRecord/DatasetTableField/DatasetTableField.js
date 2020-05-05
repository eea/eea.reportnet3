export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    pk,
    pkMustBeUsed,
    pkReferenced,
    name,
    recordId,
    referencedField,
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
    this.pkMustBeUsed = pkMustBeUsed;
    this.pkReferenced = pkReferenced;
    this.name = name;
    this.recordId = recordId;
    this.referencedField = referencedField;
    this.required = required;
    this.type = type;
    this.validations = validations;
    this.value = value;
  }
}
