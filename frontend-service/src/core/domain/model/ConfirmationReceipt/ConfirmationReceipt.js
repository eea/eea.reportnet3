export class ConfirmationReceipt {
  constructor(id, representative, dataflowName, datasets, isLastVersionDownloaded) {
    this.id = id;
    this.representative = representative;
    this.dataflowName = dataflowName;
    this.datasets = datasets;
    this.isLastVersionDownloaded = isLastVersionDownloaded;
  }
}
