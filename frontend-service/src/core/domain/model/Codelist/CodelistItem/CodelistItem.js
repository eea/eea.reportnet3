export class CodelistItem {
  constructor(id, shortCode, label, definition, codelistId) {
    this.codelistId = codelistId;
    this.definition = definition;
    this.id = id;
    this.label = label;
    this.shortCode = shortCode;
  }
}
