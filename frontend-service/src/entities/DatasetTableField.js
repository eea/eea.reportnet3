export class DatasetTableField {
  constructor({
    codelistItems,
    description,
    fieldId,
    fieldSchemaId,
    label,
    maxSize,
    name,
    pk,
    pkHasMultipleValues,
    pkMustBeUsed,
    pkReferenced,
    readOnly,
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
    this.label = label;
    this.maxSize = maxSize;
    this.name = name;
    this.pk = pk;
    this.pkHasMultipleValues = pkHasMultipleValues;
    this.pkMustBeUsed = pkMustBeUsed;
    this.pkReferenced = pkReferenced;
    this.readOnly = readOnly;
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
