export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    maxSize,
    name,
    pk,
    pkHasMultipleValues,
    pkMustBeUsed,
    pkReferenced,
    recordId,
    referencedField,
    required,
    type,
    unique,
    validations,
    validExtensions,
    value
  } = {}) {
    this.codelistItems = codelistItems;
    this.description = description;
    this.fieldId = fieldId;
    this.fieldSchemaId = fieldSchemaId;
    this.maxSize = maxSize;
    this.name = name;
    this.pk = pk;
    this.pkHasMultipleValues = pkHasMultipleValues;
    this.pkMustBeUsed = pkMustBeUsed;
    this.pkReferenced = pkReferenced;
    this.recordId = recordId;
    this.referencedField = referencedField;
    this.required = required;
    this.type = type;
    this.unique = unique;
    this.validations = validations;
    this.validExtensions = validExtensions;
    this.value = value;
  }
}
