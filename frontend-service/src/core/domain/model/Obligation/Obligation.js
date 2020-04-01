export class Obligation {
  constructor({
    comment,
    countries,
    description,
    expirationDate,
    issues,
    legalInstruments,
    obligationId,
    organization,
    title,
    validSince,
    validTo
  } = {}) {
    this.comment = comment;
    this.countries = countries;
    this.description = description;
    this.expirationDate = expirationDate;
    this.issues = issues;
    this.legalInstruments = legalInstruments;
    this.obligationId = obligationId;
    this.organization = organization;
    this.title = title;
    this.validSince = validSince;
    this.validTo = validTo;
  }
}
