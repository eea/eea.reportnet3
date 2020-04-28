import { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { routes } from 'ui/routes';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowData,
  dataflowId,
  handleRedirect,
  isCustodian,
  onLoadReceiptData,
  dataflowState,
  representative,
  onShowSnapshotDialog,
  hasWritePermissions
}) => {
  const resources = useContext(ResourcesContext);
  const helpButton = {
    layout: 'defaultBigButton',
    buttonClass: 'dataflowHelp',
    buttonIcon: 'info',
    caption: resources.messages['dataflowHelp'],
    handleRedirect: () =>
      handleRedirect(
        getUrl(
          routes.DOCUMENTS,
          {
            dataflowId: dataflowId
          },
          true
        )
      ),
    helpClassName: 'dataflow-documents-weblinks-help-step',
    onWheel: getUrl(
      routes.DOCUMENTS,
      {
        dataflowId: dataflowId
      },
      true
    ),
    visibility: true
  };

  const groupByRepresentativeModels = dataflowData.datasets
    .filter(dataset => dataset.datasetSchemaName === representative)
    .map(dataset => {
      const datasetName = dataset.name || representative;
      return {
        layout: 'defaultBigButton',
        buttonClass: 'dataset',
        buttonIcon: 'dataset',
        caption: datasetName,
        infoStatus: dataset.isReleased,
        infoStatusIcon: dataset.isReleased,
        handleRedirect: () => {
          handleRedirect(
            getUrl(
              routes.DATASET,
              {
                dataflowId: dataflowId,
                datasetId: dataset.datasetId
              },
              true
            )
          );
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        onWheel: getUrl(
          routes.DATASET,
          {
            dataflowId: dataflowId,
            datasetId: dataset.datasetId
          },
          true
        ),
        model: hasWritePermissions
          ? // model: dataflowState.hasWritePermissions
            [
              {
                label: resources.messages['releaseDataCollection'],
                icon: 'cloudUpload',
                command: () => onShowSnapshotDialog(dataset.datasetId),
                disabled: false
              }
            ]
          : [
              {
                label: resources.messages['properties'],
                icon: 'info',
                disabled: true
              }
            ],
        visibility: !isEmpty(dataflowData.datasets)
      };
    });

  const receiptBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      buttonIconClass: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      caption: resources.messages['confirmationReceipt'],
      handleRedirect: dataflowState.isReceiptLoading ? () => {} : () => onLoadReceiptData(),
      infoStatus: dataflowState.isReceiptOutdated,
      layout: 'defaultBigButton',
      visibility:
        !isCustodian &&
        !isUndefined(dataflowState.isReleased) &&
        !dataflowState.isReleased.includes(false) &&
        !dataflowState.isReleased.includes(null)
    }
  ];

  return [helpButton, ...groupByRepresentativeModels, ...receiptBigButton];
};

export { useBigButtonList };
