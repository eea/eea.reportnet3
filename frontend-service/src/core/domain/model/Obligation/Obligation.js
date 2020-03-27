export class Obligation {
  constructor({
    comment,
    description,
    legalInstruments,
    nextDeadline,
    obligationId,
    oblTitle,
    validSince,
    validTo
  } = {}) {
    this.comment = comment;
    this.description = description;
    this.legalInstruments = legalInstruments;
    this.nextDeadline = nextDeadline;
    this.obligationId = obligationId;
    this.oblTitle = oblTitle;
    this.validSince = validSince;
    this.validTo = validTo;
  }
}
