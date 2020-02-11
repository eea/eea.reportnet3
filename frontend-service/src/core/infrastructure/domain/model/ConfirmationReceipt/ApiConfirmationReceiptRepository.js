import { confirmationReceipt } from 'core/domain/model/ConfirmationReceipt/ApiConfirmationReceipt';
import { ConfirmationReceipt } from 'core/domain/model/ConfirmationReceipt/ConfirmationReceipt';

const get = async dataflowId => {
  const confirmationReceiptDTO = await confirmationReceipt.get(dataflowId);

  const datasets = confirmationReceiptDTO
    ? confirmationReceiptDTO.datasets.map(dataset => ({ name: dataset.name, releaseDate: dataset.releaseDate }))
    : [];

  const confirmationReceipt =
    confirmationReceiptDTO &&
    confirmationReceiptDTO.map(
      confirmationReceiptDTO =>
        new ConfirmationReceipt(
          confirmationReceiptDTO.id,
          confirmationReceiptDTO.representative,
          confirmationReceiptDTO.dataflowName,
          datasets,
          confirmationReceiptDTO.isLastVersionDownloaded
        )
    );

  return confirmationReceipt;
};

export const ApiSnapshotRepository = {
  get
};
