import { useContext } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { getUrl } from 'core/infrastructure/CoreUtils';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useBigButtonList = ({
  dataflowData,
  dataflowId,
  dataflowStatus,
  getDeleteSchemaIndex,
  handleRedirect,
  hasWritePermissions,
  isCustodian,
  onDatasetSchemaNameError,
  onDuplicateName,
  onSaveName,
  onShowDataCollectionModal,
  onShowNewSchemaDialog,
  showReleaseSnapshotDialog,
  updatedDatasetSchema
}) => {
  const resources = useContext(ResourcesContext);

  const buttonList = [
    {
      layout: 'menuBigButton',
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      caption: resources.messages['newItem'],
      model: [
        {
          label: resources.messages['createNewEmptyDatasetSchema'],
          icon: 'add',
          command: () => onShowNewSchemaDialog()
        },
        {
          label: resources.messages['createNewDatasetFromTemplate'],
          icon: 'add',
          disabled: true
        }
      ],
      visibility: isCustodian && dataflowStatus === config.dataflowStatus['DESIGN']
    },
    {
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
    }
  ];

  const designDatasetModels = dataflowData.designDatasets.map(newDatasetSchema => ({
    layout: 'defaultBigButton',
    buttonClass: 'designDataset',
    buttonIcon: 'pencilRuler',
    caption: newDatasetSchema.datasetSchemaName,
    dataflowStatus: dataflowStatus,
    datasetSchemaInfo: updatedDatasetSchema,
    handleRedirect: () => {
      handleRedirect(
        getUrl(
          routes.DATASET_SCHEMA,
          {
            dataflowId: dataflowId,
            datasetId: newDatasetSchema.datasetId
          },
          true
        )
      );
    },
    index: newDatasetSchema.index,
    onDuplicateName: onDuplicateName,
    onSaveError: onDatasetSchemaNameError,
    onSaveName: onSaveName,
    onWheel: getUrl(
      routes.DATASET_SCHEMA,
      {
        dataflowId: dataflowId,
        datasetId: newDatasetSchema.datasetId
      },
      true
    ),
    placeholder: resources.messages['datasetSchemaNamePlaceholder'],
    model: [
      {
        label: resources.messages['openDataset'],
        icon: 'openFolder',
        command: () => {
          handleRedirect(
            getUrl(
              routes.DATASET_SCHEMA,
              {
                dataflowId: dataflowId,
                datasetId: newDatasetSchema.datasetId
              },
              true
            )
          );
        }
      },
      {
        label: resources.messages['rename'],
        icon: 'pencil',
        disabled: dataflowStatus !== config.dataflowStatus['DESIGN']
      },
      {
        label: resources.messages['duplicate'],
        icon: 'clone',
        disabled: true
      },
      {
        label: resources.messages['delete'],
        icon: 'trash',
        disabled: dataflowStatus !== config.dataflowStatus['DESIGN'],
        command: () => getDeleteSchemaIndex(newDatasetSchema.index)
      },
      {
        label: resources.messages['properties'],
        icon: 'info',
        disabled: true
      }
    ],
    visibility: !isUndefined(dataflowData.designDatasets) && isEmpty(dataflowData.dataCollections)
  }));

  const datasetModels = dataflowData.datasets.map(dataset => ({
    layout: 'defaultBigButton',
    buttonClass: 'dataset',
    buttonIcon: 'dataset',
    caption: dataset.datasetSchemaName,
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
  }));

  const dashboardModels = [
    {
      layout: 'defaultBigButton',
      buttonClass: 'dashboard',
      buttonIcon: 'barChart',
      caption: resources.messages['dashboards'],
      handleRedirect: () =>
        handleRedirect(
          getUrl(
            routes.DASHBOARDS,
            {
              dataflowId: dataflowId
            },
            true
          )
        ),
      onWheel: getUrl(
        routes.DASHBOARDS,
        {
          dataflowId: dataflowId
        },
        true
      ),
      visibility: isCustodian && !isEmpty(dataflowData.datasets)
    }
  ];

  const createDataCollection = [
    {
      layout: 'defaultBigButton',
      buttonClass: 'createDataCollection',
      buttonIcon: 'checkedSquare',
      caption: resources.messages['createDataCollection'],
      handleRedirect: () => onShowDataCollectionModal(),
      visibility: isEmpty(dataflowData.dataCollections) && !isEmpty(dataflowData.designDatasets)
    }
  ];

  const dataCollectionModels = dataflowData.dataCollections.map(dataCollection => ({
    layout: 'defaultBigButton',
    buttonClass: 'dataCollection',
    buttonIcon: 'dataCollectionIcon',
    caption: dataCollection.dataCollectionName,
    model: [
      {
        label: resources.messages['rename'],
        icon: 'pencil',
        disabled: true
      },
      {
        label: resources.messages['duplicate'],
        icon: 'clone',
        disabled: true
      },
      {
        label: resources.messages['delete'],
        icon: 'trash',
        disabled: true
      },
      {
        label: resources.messages['properties'],
        icon: 'info',
        disabled: true
      }
    ],
    visibility: !isEmpty(dataflowData.dataCollections)
  }));

  return [
    ...buttonList,
    ...designDatasetModels,
    ...dataCollectionModels,
    ...dashboardModels,
    ...datasetModels,
    ...createDataCollection
  ];
};

export { useBigButtonList };
