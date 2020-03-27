export class Obligation {
  constructor({
    obligationId,
    oblTitle,
    description,
    validSince,
    validTo,
    comment,
    nextDeadline,
    legalInstruments
  } = {}) {
    this.obligationId = obligationId;
    this.oblTitle = oblTitle;
    this.description = description;
    this.validSince = validSince;
    this.validTo = validTo;
    this.comment = comment;
    this.nextDeadline = nextDeadline;
    this.legalInstruments = legalInstruments;
  }
}
