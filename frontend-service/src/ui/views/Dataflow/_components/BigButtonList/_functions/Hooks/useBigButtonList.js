import { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowState,
  dataflowId,
  exportDatatableSchema,
  getDeleteSchemaIndex,
  handleRedirect,
  onShowUpdateDataCollectionModal,
  isActiveButton,
  onDatasetSchemaNameError,
  onDuplicateName,
  onLoadReceiptData,
  onSaveName,
  onShowDataCollectionModal,
  onShowNewSchemaDialog,
  onShowSnapshotDialog
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
        { label: resources.messages['createNewDatasetFromTemplate'], icon: 'add', disabled: true }
      ],
      visibility: dataflowState.isCustodian && dataflowState.status === DataflowConf.dataflowStatus['DESIGN']
    },
    {
      buttonClass: 'dataflowHelp',
      buttonIcon: 'info',
      caption: resources.messages['dataflowHelp'],
      layout: 'defaultBigButton',
      handleRedirect: () => handleRedirect(getUrl(routes.DOCUMENTS, { dataflowId }, true)),
      helpClassName: 'dataflow-documents-weblinks-help-step',
      onWheel: getUrl(routes.DOCUMENTS, { dataflowId }, true),
      visibility: true
    }
  ];

  const designDatasetModels = dataflowState.data.designDatasets.map(newDatasetSchema => ({
    buttonClass: 'schemaDataset',
    buttonIcon: 'pencilRuler',
    caption: newDatasetSchema.datasetSchemaName,
    dataflowStatus: dataflowState.status,
    datasetSchemaInfo: dataflowState.updatedDatasetSchema,
    handleRedirect: () => {
      handleRedirect(getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true));
    },
    helpClassName: 'dataflow-schema-help-step',
    index: newDatasetSchema.index,
    layout: 'defaultBigButton',
    model: [
      {
        label: resources.messages['openDataset'],
        icon: 'openFolder',
        command: () => {
          handleRedirect(getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true));
        }
      },
      {
        label: resources.messages['rename'],
        icon: 'pencil',
        disabled: dataflowState.status !== DataflowConf.dataflowStatus['DESIGN']
      },
      {
        label: resources.messages['delete'],
        icon: 'trash',
        disabled: dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'],
        command: () => getDeleteSchemaIndex(newDatasetSchema.index)
      }
      // {
      //   label: resources.messages['exportDatasetSchema'],
      //   icon: 'import',
      //   // disabled: dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'],
      //   command: () => exportDatatableSchema(newDatasetSchema.datasetId, newDatasetSchema.datasetSchemaName)
      // }
    ],
    onDuplicateName: onDuplicateName,
    onSaveError: onDatasetSchemaNameError,
    onSaveName: onSaveName,
    onWheel: getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true),
    placeholder: resources.messages['datasetSchemaNamePlaceholder'],
    visibility:
      !isUndefined(dataflowState.data.designDatasets) &&
      isEmpty(dataflowState.data.dataCollections) &&
      dataflowState.isCustodian
  }));

  const buildGroupByRepresentativeModels = dataflowData => {
    const { datasets } = dataflowData;

    const uniqRepresentatives = uniq(
      datasets.map(dataset => {
        return { name: dataset.datasetSchemaName, id: dataset.dataProviderId };
      })
    );

    if (uniqRepresentatives.length === 1 && !dataflowState.isCustodian) {
      // const [representative] = uniqRepresentatives;

      return datasets.map(dataset => {
        const datasetName = dataset.name;
        return {
          buttonClass: 'dataset',
          buttonIcon: 'dataset',
          caption: datasetName,
          helpClassName: 'dataflow-dataset-help-step',
          handleRedirect: () => {
            handleRedirect(getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true));
          },
          infoStatus: dataset.isReleased,
          infoStatusIcon: true,
          layout: 'defaultBigButton',
          model: dataflowState.hasWritePermissions
            ? [
                {
                  label: resources.messages['releaseDataCollection'],
                  icon: 'cloudUpload',
                  command: () => onShowSnapshotDialog(dataset.datasetId),
                  disabled: false
                }
              ]
            : [{ label: resources.messages['properties'], icon: 'info', disabled: true }],
          onWheel: getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true),
          visibility: !isEmpty(dataflowState.data.datasets)
        };
      });
    }
    return uniqRepresentatives.map(representative => ({
      buttonClass: 'dataset',
      buttonIcon: 'representative',
      caption: representative.name,
      handleRedirect: () => {
        handleRedirect(getUrl(routes.REPRESENTATIVE, { dataflowId, representative: representative.name }, true));
        // window.history.replaceState(null, '', `/dataflow/${dataflowId}/${representative.id}`);
      },
      helpClassName: 'dataflow-dataset-container-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.REPRESENTATIVE, { dataflowId, representative: representative.id }, true),
      visibility: !isEmpty(dataflowState.data.datasets)
    }));
  };

  const groupByRepresentativeModels = buildGroupByRepresentativeModels(dataflowState.data);

  const dashboardModels = [
    {
      buttonClass: 'dashboard',
      buttonIcon: 'barChart',
      caption: resources.messages['dashboards'],
      handleRedirect: () => handleRedirect(getUrl(routes.DASHBOARDS, { dataflowId }, true)),
      helpClassName: 'dataflow-dashboards-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.DASHBOARDS, { dataflowId }, true),
      visibility: dataflowState.isCustodian && !isEmpty(dataflowState.data.datasets)
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
        isEmpty(dataflowState.data.dataCollections) &&
        dataflowState.isDataSchemaCorrect &&
        dataflowState.formHasRepresentatives
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
      visibility: dataflowState.status === 'DRAFT' && dataflowState.hasRepresentativesWithoutDatasets
    }
  ];

  const dataCollectionModels = dataflowState.data.dataCollections.map(dataCollection => ({
    buttonClass: 'schemaDataset',
    buttonIcon: 'dataCollection',
    caption: dataCollection.dataCollectionName,
    handleRedirect: () => {
      handleRedirect(getUrl(routes.DATA_COLLECTION, { dataflowId, datasetId: dataCollection.dataCollectionId }, true));
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
    visibility: !isEmpty(dataflowState.data.dataCollections)
  }));

  const onBuildReceiptButton = () => {
    const { datasets } = dataflowState.data;
    const representativeNames = datasets.map(dataset => dataset.datasetSchemaName);
    const releasedStates = datasets.map(dataset => dataset.isReleased);

    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
        buttonIconClass: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
        caption: resources.messages['confirmationReceipt'],
        handleRedirect: dataflowState.isReceiptLoading ? () => {} : () => onLoadReceiptData(),
        infoStatus: dataflowState.isReceiptOutdated,
        layout: 'defaultBigButton',
        visibility:
          !dataflowState.isCustodian &&
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
