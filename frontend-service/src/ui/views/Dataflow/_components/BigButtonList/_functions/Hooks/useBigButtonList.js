import { useContext } from 'react';

import { isEmpty, isUndefined, uniq } from 'lodash';

import DataflowConf from 'conf/dataflow.config.json';
import { routes } from 'ui/routes';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowData,
  dataflowId,
  dataflowStatus,
  getDeleteSchemaIndex,
  handleRedirect,
  hasRepresentatives,
  isCreateButtonActive,
  isCustodian,
  isDataSchemaCorrect,
  onDatasetSchemaNameError,
  onDuplicateName,
  onLoadReceiptData,
  onSaveName,
  onShowDataCollectionModal,
  onShowNewSchemaDialog,
  updatedDatasetSchema,
  hasWritePermissions,
  showReleaseSnapshotDialog
}) => {
  const resources = useContext(ResourcesContext);

  const buttonList = [
    {
      layout: 'menuBigButton',
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      buttonIconClass: 'newItemCross',
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
      visibility: isCustodian && dataflowStatus === DataflowConf.dataflowStatus['DESIGN']
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
    buttonClass: 'schemaDataset',
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
        disabled: dataflowStatus !== DataflowConf.dataflowStatus['DESIGN']
      },
      {
        label: resources.messages['duplicate'],
        icon: 'clone',
        disabled: true
      },
      {
        label: resources.messages['delete'],
        icon: 'trash',
        disabled: dataflowStatus !== DataflowConf.dataflowStatus['DESIGN'],
        command: () => getDeleteSchemaIndex(newDatasetSchema.index)
      },
      {
        label: resources.messages['properties'],
        icon: 'info',
        disabled: true
      }
    ],
    visibility: !isUndefined(dataflowData.designDatasets) && isEmpty(dataflowData.dataCollections) && isCustodian
  }));

  const buildGroupByRepresentativeModels = dataflowData => {
    const { datasets } = dataflowData;
    const representatives = datasets.map(dataset => {
      return dataset.datasetSchemaName;
    });
    const uniqRepresentatives = uniq(representatives);
    if (uniqRepresentatives.length === 1 && !isCustodian) {
      const [representative] = uniqRepresentatives;
      return datasets.map(dataset => {
        const datasetName = dataset.name;
        return {
          layout: 'defaultBigButton',
          buttonClass: 'dataset',
          buttonIcon: 'dataset',
          caption: datasetName,
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
          infoStatus: dataset.isReleased,
          infoStatusIcon: true,
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
    }
    return uniqRepresentatives.map(representative => ({
      layout: 'defaultBigButton',
      buttonClass: 'dataset',
      buttonIcon: 'representative',
      caption: representative,
      handleRedirect: () => {
        handleRedirect(
          getUrl(
            routes.REPRESENTATIVE,
            {
              dataflowId: dataflowId,
              representative: representative
            },
            true
          )
        );
      },
      onWheel: getUrl(
        routes.REPRESENTATIVE,
        {
          dataflowId: dataflowId,
          representative: representative
        },
        true
      ),
      visibility: !isEmpty(dataflowData.datasets)
    }));
  };

  const groupByRepresentativeModels = buildGroupByRepresentativeModels(dataflowData);

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
      buttonClass: 'newItem',
      buttonIcon: isCreateButtonActive ? 'siteMap' : 'spinner',
      buttonIconClass: isCreateButtonActive ? 'siteMap' : 'spinner',
      caption: resources.messages['createDataCollection'],
      handleRedirect: isCreateButtonActive ? () => onShowDataCollectionModal() : () => {},
      visibility: isEmpty(dataflowData.dataCollections) && isDataSchemaCorrect && hasRepresentatives
    }
  ];

  const dataCollectionModels = dataflowData.dataCollections.map(dataCollection => ({
    layout: 'defaultBigButton',
    buttonClass: 'schemaDataset',
    buttonIcon: 'dataCollection',
    caption: dataCollection.dataCollectionName,
    handleRedirect: () => {
      handleRedirect(
        getUrl(
          routes.DATA_COLLECTION,
          {
            dataflowId: dataflowId,
            datasetId: dataCollection.dataCollectionId
          },
          true
        )
      );
    },
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

  const onBuildReceiptButton = () => {
    const { datasets } = dataflowData;
    const representatives = datasets.map(dataset => {
      return dataset.datasetSchemaName;
    });
    const isReleased = datasets.map(dataset => {
      return dataset.isReleased;
    });
    return [
      {
        layout: 'defaultBigButton',
        buttonClass: 'schemaDataset',
        buttonIcon: false ? 'spinner' : 'fileDownload',
        buttonIconClass: false ? 'spinner' : '',
        caption: resources.messages['confirmationReceipt'],
        handleRedirect: () => onLoadReceiptData(),
        visibility: !isCustodian && uniq(representatives).length === 1 && !isReleased.includes(false)
      }
    ];
  };

  const receiptBigButton = onBuildReceiptButton();

  return [
    ...buttonList,
    ...designDatasetModels,
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...createDataCollection,
    ...dataCollectionModels,
    ...dashboardModels
  ];
};

export { useBigButtonList };
