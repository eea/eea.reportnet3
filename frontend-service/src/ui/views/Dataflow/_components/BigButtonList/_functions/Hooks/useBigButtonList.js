import { useContext } from 'react';

import { isEmpty, isUndefined, uniq } from 'lodash';

import DataflowConf from 'conf/dataflow.config.json';
import { routes } from 'ui/routes';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowDataState,
  dataflowData,
  dataflowId,
  dataflowStatus,
  getDeleteSchemaIndex,
  handleRedirect,
  onShowUpdateDataCollectionModal,
  hasWritePermissions,
  isActiveButton,
  isCustodian,
  isDataSchemaCorrect,
  onDatasetSchemaNameError,
  onDuplicateName,
  onLoadReceiptData,
  onSaveName,
  onShowDataCollectionModal,
  onShowNewSchemaDialog,
  receiptState,
  showReleaseSnapshotDialog,
  updatedDatasetSchema
}) => {
  const resources = useContext(ResourcesContext);

  const buttonList = [
    {
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      buttonIconClass: 'newItemCross',
      caption: resources.messages['newItem'],
      helpClassName: 'dataflow-new-item-help-step',
      layout: 'menuBigButton',
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
      buttonClass: 'dataflowHelp',
      buttonIcon: 'info',
      caption: resources.messages['dataflowHelp'],
      layout: 'defaultBigButton',
      handleRedirect: () =>
        handleRedirect(
          getUrl(
            routes.DOCUMENTS,
            {
              dataflowId
            },
            true
          )
        ),
      helpClassName: 'dataflow-documents-weblinks-help-step',
      onWheel: getUrl(
        routes.DOCUMENTS,
        {
          dataflowId
        },
        true
      ),
      visibility: true
    }
  ];

  const designDatasetModels = dataflowData.designDatasets.map(newDatasetSchema => ({
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
            dataflowId,
            datasetId: newDatasetSchema.datasetId
          },
          true
        )
      );
    },
    helpClassName: 'dataflow-schema-help-step',
    index: newDatasetSchema.index,
    layout: 'defaultBigButton',
    model: [
      {
        label: resources.messages['openDataset'],
        icon: 'openFolder',
        command: () => {
          handleRedirect(
            getUrl(
              routes.DATASET_SCHEMA,
              {
                dataflowId,
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
    onDuplicateName: onDuplicateName,
    onSaveError: onDatasetSchemaNameError,
    onSaveName: onSaveName,
    onWheel: getUrl(
      routes.DATASET_SCHEMA,
      {
        dataflowId,
        datasetId: newDatasetSchema.datasetId
      },
      true
    ),
    placeholder: resources.messages['datasetSchemaNamePlaceholder'],
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
          buttonClass: 'dataset',
          buttonIcon: 'dataset',
          caption: datasetName,
          helpClassName: 'dataflow-dataset-help-step',
          handleRedirect: () => {
            handleRedirect(
              getUrl(
                routes.DATASET,
                {
                  dataflowId,
                  datasetId: dataset.datasetId
                },
                true
              )
            );
          },
          infoStatus: dataset.isReleased,
          infoStatusIcon: true,
          layout: 'defaultBigButton',
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
          onWheel: getUrl(
            routes.DATASET,
            {
              dataflowId,
              datasetId: dataset.datasetId
            },
            true
          ),
          visibility: !isEmpty(dataflowData.datasets)
        };
      });
    }
    return uniqRepresentatives.map(representative => ({
      buttonClass: 'dataset',
      buttonIcon: 'representative',
      caption: representative,
      handleRedirect: () => {
        handleRedirect(
          getUrl(
            routes.REPRESENTATIVE,
            {
              dataflowId,
              representative
            },
            true
          )
        );
      },
      helpClassName: 'dataflow-dataset-container-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(
        routes.REPRESENTATIVE,
        {
          dataflowId,
          representative
        },
        true
      ),
      visibility: !isEmpty(dataflowData.datasets)
    }));
  };

  const groupByRepresentativeModels = buildGroupByRepresentativeModels(dataflowData);

  const dashboardModels = [
    {
      buttonClass: 'dashboard',
      buttonIcon: 'barChart',
      caption: resources.messages['dashboards'],
      handleRedirect: () =>
        handleRedirect(
          getUrl(
            routes.DASHBOARDS,
            {
              dataflowId
            },
            true
          )
        ),
      helpClassName: 'dataflow-dashboards-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(
        routes.DASHBOARDS,
        {
          dataflowId
        },
        true
      ),
      visibility: isCustodian && !isEmpty(dataflowData.datasets)
    }
  ];

  const createDataCollection = [
    {
      buttonClass: 'newItem',
      buttonIcon: isActiveButton ? 'siteMap' : 'spinner',
      buttonIconClass: isActiveButton ? 'siteMap' : 'spinner',
      caption: resources.messages['createDataCollection'],
      helpClassName: 'dataflow-datacollection-help-step',
      handleRedirect: isActiveButton ? () => onShowDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      visibility:
        isEmpty(dataflowData.dataCollections) &&
        isDataSchemaCorrect &&
        dataflowDataState.hasRepresentativesWithoutDatasets
    }
  ];

  const updateDatasetsNewRepresentatives = [
    {
      buttonClass: 'newItem',
      buttonIcon: isActiveButton ? 'siteMap' : 'spinner',
      buttonIconClass: isActiveButton ? 'siteMap' : 'spinner',
      caption: resources.messages['updateDataCollection'],
      helpClassName: 'dataflow-datacollection-help-step',
      handleRedirect: isActiveButton ? () => onShowUpdateDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      visibility: dataflowDataState.status === 'DRAFT' && dataflowDataState.hasRepresentativesWithoutDatasets
    }
  ];

  const dataCollectionModels = dataflowData.dataCollections.map(dataCollection => ({
    buttonClass: 'schemaDataset',
    buttonIcon: 'dataCollection',
    caption: dataCollection.dataCollectionName,
    handleRedirect: () => {
      handleRedirect(
        getUrl(
          routes.DATA_COLLECTION,
          {
            dataflowId,
            datasetId: dataCollection.dataCollectionId
          },
          true
        )
      );
    },
    helpClassName: 'dataflow-datacollection-help-step',
    layout: 'defaultBigButton',
    // model: [
    //   {
    //     label: resources.messages['rename'],
    //     icon: 'pencil',
    //     disabled: true
    //   },
    //   {
    //     label: resources.messages['duplicate'],
    //     icon: 'clone',
    //     disabled: true
    //   },
    //   {
    //     label: resources.messages['delete'],
    //     icon: 'trash',
    //     disabled: true
    //   },
    //   {
    //     label: resources.messages['properties'],
    //     icon: 'info',
    //     disabled: true
    //   }
    // ],
    visibility: !isEmpty(dataflowData.dataCollections)
  }));

  const onBuildReceiptButton = () => {
    const { datasets } = dataflowData;
    const representativeNames = datasets.map(dataset => {
      return dataset.datasetSchemaName;
    });
    const releasedStates = datasets.map(dataset => {
      return dataset.isReleased;
    });

    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: receiptState.isLoading ? 'spinner' : 'fileDownload',
        buttonIconClass: receiptState.isLoading ? 'spinner' : 'fileDownload',
        caption: resources.messages['confirmationReceipt'],
        handleRedirect: receiptState.isLoading ? () => {} : () => onLoadReceiptData(),
        infoStatus: receiptState.isOutdated,
        layout: 'defaultBigButton',
        visibility:
          !isCustodian &&
          uniq(representativeNames).length === 1 &&
          !releasedStates.includes(false) &&
          !releasedStates.includes(null)
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
    ...dashboardModels,
    ...updateDatasetsNewRepresentatives
  ];
};

export { useBigButtonList };
