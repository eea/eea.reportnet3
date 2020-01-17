import { useContext } from 'react';

import { isEmpty, isUndefined, uniq } from 'lodash';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { getUrl } from 'core/infrastructure/CoreUtils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useBigButtonList = ({
  dataflowData,
  dataflowId,
  handleRedirect,
  hasWritePermissions,
  showReleaseSnapshotDialog,
  representative
}) => {
  const resources = useContext(ResourcesContext);
  console.log(dataflowData);

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
      const { designDatasets } = dataflowData;
      const datasetName =
        !isUndefined(designDatasets) && designDatasets.length > 0
          ? designDatasets.find(designDataset => designDataset.datasetSchemaId === dataset.datasetSchemaId)
              .datasetSchemaName
          : representative;
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
  return [helpButton, ...groupByRepresentativeModels];
};

export { useBigButtonList };
