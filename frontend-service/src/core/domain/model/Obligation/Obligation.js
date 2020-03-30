export class Obligation {
  constructor({
    client,
    comment,
    countries,
    description,
    expirationDate,
    issues,
    legalInstruments,
    obligationId,
    title,
    validSince,
    validTo
  } = {}) {
    this.client = client;
    this.comment = comment;
    this.countries = countries;
    this.description = description;
    this.expirationDate = expirationDate;
    this.issues = issues;
    this.legalInstruments = legalInstruments;
    this.obligationId = obligationId;
    this.title = title;
    this.validSince = validSince;
    this.validTo = validTo;
  }
}
