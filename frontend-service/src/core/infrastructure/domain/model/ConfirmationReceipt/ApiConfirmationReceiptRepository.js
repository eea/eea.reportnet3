import { apiConfirmationReceipt } from 'core/infrastructure/api/domain/model/ConfirmationReceipt/ApiConfirmationReceipt';
import { ConfirmationReceipt } from 'core/domain/model/ConfirmationReceipt/ConfirmationReceipt';

const get = async (dataflowId, dataProviderId) => {
  const confirmationReceipt = await apiConfirmationReceipt.get(dataflowId, dataProviderId);

  // const datasets = confirmationReceiptDTO
  //   ? confirmationReceiptDTO.datasets.map(dataset => ({
  //       name: dataset.nameDatasetSchema,
  //       releaseDate: dataset.dateReleased
  //     }))
  //   : [];
  // const confirmationReceipt = new ConfirmationReceipt({
  //   dataflowId: confirmationReceiptDTO.idDataflow,
  //   dataflowName: confirmationReceiptDTO.dataflowName,
  //   datasets: datasets,
  //   representative: confirmationReceiptDTO.providerAssignation,
  //   representativeEmail: confirmationReceiptDTO.providerEmail
  // });

  return confirmationReceipt;
};

export const ApiConfirmationReceiptRepository = {
  get
};
