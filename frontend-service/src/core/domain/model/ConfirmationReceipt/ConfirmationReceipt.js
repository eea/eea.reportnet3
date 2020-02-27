export class ConfirmationReceipt {
  constructor({ dataflowId, dataflowName, datasets, representative, representativeEmail }) {
    this.dataflowId = dataflowId;
    this.dataflowName = dataflowName;
    this.datasets = datasets;
    this.representative = representative;
    this.representativeEmail = representativeEmail;
  }
}
