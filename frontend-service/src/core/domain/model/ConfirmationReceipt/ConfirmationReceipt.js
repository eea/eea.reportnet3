export class ConfirmationReceipt {
  constructor({ dataflowId, representative, dataflowName, datasets }) {
    this.dataflowId = dataflowId;
    this.dataflowName = dataflowName;
    this.datasets = datasets;
    this.representative = representative;
  }
}
