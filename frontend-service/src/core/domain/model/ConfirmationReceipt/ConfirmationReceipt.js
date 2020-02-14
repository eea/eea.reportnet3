export class ConfirmationReceipt {
  constructor(id, representative, dataflowName, datasets) {
    this.id = id;
    this.representative = representative;
    this.dataflowName = dataflowName;
    this.datasets = datasets;
  }
}
