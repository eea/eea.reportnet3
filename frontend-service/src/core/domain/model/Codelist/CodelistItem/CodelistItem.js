export class CodelistItem {
  constructor({ codelistId, definition, id, label, shortCode } = {}) {
    this.codelistId = codelistId;
    this.definition = definition;
    this.id = id;
    this.label = label;
    this.shortCode = shortCode;
  }
}
