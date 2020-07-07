import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniq from 'lodash/uniq';

import { config } from 'conf';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowId,
  dataflowState,
  // exportDatatableSchema,
  getDeleteSchemaIndex,
  handleRedirect,
  isActiveButton,
  onCloneDataflow,
  onDatasetSchemaNameError,
  onDuplicateName,
  onLoadReceiptData,
  onSaveName,
  onShowDataCollectionModal,
  onShowManageReportersDialog,
  onShowNewSchemaDialog,
  onShowSnapshotDialog,
  onShowUpdateDataCollectionModal
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.DATAFLOW}${dataflowId}`);
      // setButtonsVisibility(getButtonsVisibility(userRoles.map(userRole => config.permissions[userRole])));
      setButtonsVisibility(
        getButtonsVisibility(['LEAD_REPORTER', 'REPORTER_READ'].map(userRole => config.permissions[userRole]))
      );
    }
  }, [userContext]);

  const getButtonsVisibility = roles => ({
    createDataCollection:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']),
    dashboard:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']) ||
      roles.includes(config.permissions['EDITOR_READ']),
    designDatasets:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']) ||
      roles.includes(config.permissions['EDITOR_READ']),
    designDatasetsActions:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']),
    groupByRepresentative:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']) ||
      roles.includes(config.permissions['EDITOR_READ']),
    manageReporters:
      (roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD'])) &&
      (!roles.includes(config.permissions['EDITOR_WRITE']) || !roles.includes(config.permissions['EDITOR_READ'])),
    newSchema:
      roles.includes(config.permissions['DATA_CUSTODIAN']) ||
      roles.includes(config.permissions['DATA_STEWARD']) ||
      roles.includes(config.permissions['EDITOR_WRITE']),
    updateReporters:
      (roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD'])) &&
      (!roles.includes(config.permissions['EDITOR_WRITE']) || !roles.includes(config.permissions['EDITOR_READ'])),
    receipt: roles.includes(config.permissions['LEAD_REPORTER']) || roles.includes(config.permissions['REPORTER']),
    release:
      roles.includes(config.permissions['LEAD_REPORTER']) &&
      !roles.includes(config.permissions['REPORTER_WRITE']) &&
      !roles.includes(config.permissions['REPORTER_READ'])
  });

  const manageReportersBigButton = [
    {
      buttonClass: 'manageReporters',
      buttonIcon: 'manageReporters',
      caption: resources.messages['manageReporters'],
      layout: 'defaultBigButton',
      handleRedirect: () => onShowManageReportersDialog(),
      visibility: buttonsVisibility.manageReporters
    }
  ];

  const helpBigButton = [
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

  const newSchemaBigButton = [
    {
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      buttonIconClass: 'newItemCross',
      // caption: resources.messages['newItem'],
      caption: resources.messages['newSchema'],
      helpClassName: 'dataflow-new-item-help-step',
      layout: 'menuBigButton',
      // layout: 'defaultBigButton',
      handleRedirect: () => onShowNewSchemaDialog(),
      model: [
        {
          label: resources.messages['createNewEmptyDatasetSchema'],
          icon: 'add',
          command: () => onShowNewSchemaDialog()
        },
        {
          label: resources.messages['cloneSchemasFromDataflow'],
          icon: 'add',
          command: () => onCloneDataflow()
        }
      ],
      visibility: buttonsVisibility.newSchema && dataflowState.status === DataflowConf.dataflowStatus['DESIGN']
    }
  ];

  const designDatasetModels = dataflowState.data.designDatasets.map(newDatasetSchema => ({
    buttonClass: 'schemaDataset',
    buttonIcon: 'pencilRuler',
    canEditName: buttonsVisibility.designDatasetsActions,
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
        disabled:
          dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'] && buttonsVisibility.designDatasetsActions
      },
      {
        label: resources.messages['delete'],
        icon: 'trash',
        disabled:
          dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'] && buttonsVisibility.designDatasetsActions,
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
      buttonsVisibility.designDatasets &&
      dataflowState.status === DataflowConf.dataflowStatus['DESIGN']
  }));

  const buildGroupByRepresentativeModels = dataflowData => {
    const { datasets } = dataflowData;

    const allDatasets = datasets.map(dataset => {
      return { name: dataset.datasetSchemaName, id: dataset.dataProviderId };
    });

    const isUniqRepresentative = uniq(allDatasets.map(dataset => dataset.id)).length === 1;

    if (isUniqRepresentative && !buttonsVisibility.groupByRepresentative) {
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
          // model: [{ label: resources.messages['properties'], icon: 'info', disabled: true }],
          onWheel: getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true),
          visibility: !isEmpty(dataflowState.data.datasets)
        };
      });
    }

    return allDatasets.map(representative => ({
      buttonClass: 'dataset',
      buttonIcon: 'representative',
      caption: representative.name,
      handleRedirect: () => {
        handleRedirect(
          getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: representative.id }, true)
        );
      },

      helpClassName: 'dataflow-dataset-container-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.REPRESENTATIVE, { dataflowId, representativeId: representative.id }, true),
      visibility: !isEmpty(dataflowState.data.datasets)
    }));
  };

  const groupByRepresentativeModels = buildGroupByRepresentativeModels(dataflowState.data);

  const checkDisabledDataCollectionButton = () =>
    isEmpty(dataflowState.data.dataCollections) &&
    dataflowState.isDataSchemaCorrect &&
    dataflowState.formHasRepresentatives;

  const dashboardBigButton = [
    {
      buttonClass: 'dashboard',
      buttonIcon: 'barChart',
      caption: resources.messages['dashboards'],
      handleRedirect: () => handleRedirect(getUrl(routes.DASHBOARDS, { dataflowId }, true)),
      helpClassName: 'dataflow-dashboards-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.DASHBOARDS, { dataflowId }, true),
      visibility: buttonsVisibility.dashboard && !isEmpty(dataflowState.data.datasets)
    }
  ];

  const createDataCollection = [
    {
      buttonClass: 'newItem',
      buttonIcon: isActiveButton ? 'siteMap' : 'spinner',
      buttonIconClass: isActiveButton
        ? !checkDisabledDataCollectionButton()
          ? 'siteMapDisabled'
          : 'siteMap'
        : 'spinner',
      caption: resources.messages['createDataCollection'],
      enabled: checkDisabledDataCollectionButton(),
      helpClassName: 'dataflow-datacollection-help-step',
      handleRedirect:
        isActiveButton && checkDisabledDataCollectionButton() ? () => onShowDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      tooltip: !isEmpty(dataflowState.data.dataCollections)
        ? resources.messages['disabledCreateDataCollectionSchemas']
        : !dataflowState.isDataSchemaCorrect
        ? resources.messages['disabledCreateDataCollectionSchemasWithError']
        : !dataflowState.formHasRepresentatives
        ? resources.messages['disabledCreateDataCollectionNoProviders']
        : undefined,
      visibility:
        isEmpty(dataflowState.data.dataCollections) &&
        dataflowState.status === 'DESIGN' &&
        buttonsVisibility.createDataCollection
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
      visibility:
        buttonsVisibility.updateReporters &&
        dataflowState.status === 'DRAFT' &&
        dataflowState.hasRepresentativesWithoutDatasets
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
          buttonsVisibility.receipt &&
          uniq(representativeNames).length === 1 &&
          !releasedStates.includes(false) &&
          !releasedStates.includes(null)
      }
    ];
  };

  const onBuildReleaseButton = () => {
    const { datasets } = dataflowState.data;

    const allDatasets = datasets.map(dataset => {
      return { name: dataset.datasetSchemaName, id: dataset.dataProviderId };
    });

    const isUniqRepresentative = uniq(allDatasets.map(dataset => dataset.id)).length === 1;

    const properties = [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: 'released',
        buttonIconClass: 'released',
        caption: resources.messages['releaseDataCollection'],
        handleRedirect: datasets.length > 1 ? () => {} : () => onShowSnapshotDialog(datasets[0].datasetId),
        layout: datasets.length > 1 ? 'menuBigButton' : 'defaultBigButton',
        visibility:
          buttonsVisibility.release &&
          dataflowState.status !== 'DESIGN' &&
          !isEmpty(dataflowState.data.datasets) &&
          isUniqRepresentative
      }
    ];

    if (datasets.length > 1) {
      properties[0].model = datasets.map(dataset => {
        return {
          label: dataset.name,
          icon: 'cloudUpload',
          command: () => onShowSnapshotDialog(dataset.datasetId),
          disabled: false
        };
      });
    }

    return properties;
  };

  const receiptBigButton = onBuildReceiptButton();

  const releaseBigButton = onBuildReleaseButton();

  return [
    ...manageReportersBigButton,
    ...helpBigButton,
    ...dashboardBigButton,
    ...dataCollectionModels,
    ...designDatasetModels,
    ...newSchemaBigButton,
    ...createDataCollection,
    ...updateDatasetsNewRepresentatives,
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...releaseBigButton
  ];
};

export { useBigButtonList };
