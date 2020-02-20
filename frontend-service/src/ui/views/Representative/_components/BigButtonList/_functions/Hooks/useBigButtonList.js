import { useContext } from 'react';

import { isEmpty, uniq } from 'lodash';

import { routes } from 'ui/routes';

import { getUrl } from 'core/infrastructure/CoreUtils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useBigButtonList = ({
  dataflowData,
  dataflowId,
  handleRedirect,
  hasWritePermissions,
  isCustodian,
  onLoadReceiptData,
  showReleaseSnapshotDialog,
  representative
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
        isReleased: dataset.isReleased,
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
        onWheel: getUrl(
          routes.DATASET,
          {
            dataflowId: dataflowId,
            datasetId: dataset.datasetId
          },
          true
        ),
        model: hasWritePermissions
          ? [
              {
                label: resources.messages['releaseDataCollection'],
                icon: 'cloudUpload',
                command: () => showReleaseSnapshotDialog(dataset.datasetId),
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

  const onBuildReceiptButton = () => {
    const { datasets } = dataflowData;
    const { representatives } = dataflowData;
    const isReleased = datasets.map(dataset => {
      return dataset.isReleased;
    });
    // const isOutdated = representatives.filter(representative => representative.dataProviderId === datasets)
    return [
      {
        layout: 'defaultBigButton',
        buttonClass: 'schemaDataset',
        buttonIcon: false ? 'spinner' : 'fileDownload',
        buttonIconClass: false ? 'spinner' : '',
        caption: resources.messages['confirmationReceipt'],
        handleRedirect: () => onLoadReceiptData(),
        visibility: !isCustodian /* && !isReleased.includes(false) */
      }
    ];
  };

  const receiptBigButton = onBuildReceiptButton();

  return [helpButton, ...groupByRepresentativeModels, ...receiptBigButton];
};

export { useBigButtonList };
