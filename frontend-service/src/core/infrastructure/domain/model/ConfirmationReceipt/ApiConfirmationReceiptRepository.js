import { apiConfirmationReceipt } from 'core/infrastructure/api/domain/model/ConfirmationReceipt/ApiConfirmationReceipt';
import { ConfirmationReceipt } from 'core/domain/model/ConfirmationReceipt/ConfirmationReceipt';

const get = async (dataflowId, dataProviderId) => {
  // const confirmationReceiptDTO = await apiConfirmationReceipt.get(dataflowId, dataProviderId);

  //   const datasets = confirmationReceiptDTO
  //     ? confirmationReceiptDTO.datasets.map(dataset => ({ name: dataset.dataSetName, releaseDate: dataset.dateReleased }))
  //     : [];

  //   const confirmationReceipt = new ConfirmationReceipt(
  //     confirmationReceiptDTO.idDataflow,
  //     confirmationReceiptDTO.providerAssignation,
  //     confirmationReceiptDTO.dataflowName,
  //     datasets
  //   );

  //   return confirmationReceipt;
  // };

  const confirmationReceiptMock = {
    idDataflow: 5061,
    dataflowName: 'DF receipt 1',
    datasets: [
      {
        id: 5833,
        dataSetName: 'Denmark',
        creationDate: 1581673921830,
        isReleased: true,
        dateReleased: 1581622608687,
        dataProviderId: 14,
        datasetSchema: '5e4668574d625428a08b2468',
        nameDatasetSchema: 's1'
      },
      {
        id: 5834,
        dataSetName: 'Denmark',
        creationDate: 1581673921826,
        isReleased: true,
        dateReleased: 1581622608687,
        dataProviderId: 14,
        datasetSchema: '5e46685c4d625428a08b2469',
        nameDatasetSchema: 's2'
      }
    ],
    providerEmail: 'vicenteprovider@reportnet.net',
    providerAssignation: 'Denmark'
  };

  const datasets = confirmationReceiptMock.datasets.map(dataset => ({
    name: dataset.dataSetName,
    releaseDate: dataset.dateReleased
  }));

  const confirmationReceipt = new ConfirmationReceipt(
    confirmationReceiptMock.idDataflow,
    confirmationReceiptMock.providerAssignation,
    confirmationReceiptMock.dataflowName,
    datasets
  );

  return confirmationReceipt;
};

export const ApiConfirmationReceiptRepository = {
  get
};
