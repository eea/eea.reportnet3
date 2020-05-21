export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    name,
    pk,
    pkMustBeUsed,
    pkReferenced,
    recordId,
    referencedField,
    required,
    type,
    unique,
    validations,
    value
  } = {}) {
    this.codelistItems = codelistItems;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.name = name;
    this.pk = pk;
    this.pkMustBeUsed = pkMustBeUsed;
    this.pkReferenced = pkReferenced;
    this.recordId = recordId;
    this.referencedField = referencedField;
    this.required = required;
    this.type = type;
    this.unique = unique;
    this.validations = validations;
    this.value = value;
  }
}
