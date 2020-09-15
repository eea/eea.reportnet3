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
  getDatasetData,
  getDataHistoricReleases,
  getDeleteSchemaIndex,
  handleExportEuDataset,
  handleRedirect,
  isActiveButton,
  onCloneDataflow,
  onLoadEuDatasetIntegration,
  onLoadReceiptData,
  onSaveName,
  onShowCopyDataCollectionToEuDatasetModal,
  onShowDataCollectionModal,
  onShowExportEuDatasetModal,
  onShowHistoricReleases,
  onShowManageReportersDialog,
  onShowNewSchemaDialog,
  onShowSnapshotDialog,
  onShowUpdateDataCollectionModal,
  setErrorDialogData
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.DATAFLOW}${dataflowId}`);
      setButtonsVisibility(getButtonsVisibility(userRoles.map(userRole => config.permissions[userRole])));
      // setButtonsVisibility(getButtonsVisibility(['LEAD_REPORTER'].map(userRole => config.permissions[userRole])));
    }
  }, [userContext]);

  const getButtonsVisibility = roles => ({
    createDataCollection:
      roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD']),
    cloneSchemasFromDataflow:
      roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD']),
    copyDataCollectionToEuDataset:
      roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD']),
    exportEuDataset:
      roles.includes(config.permissions['DATA_CUSTODIAN']) || roles.includes(config.permissions['DATA_STEWARD']),
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
      handleRedirect: () => onShowManageReportersDialog(),
      helpClassName: 'dataflow-big-buttons-manageReporters-help-step',
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.manageReporters
    }
  ];

  const helpBigButton = [
    {
      buttonClass: 'dataflowHelp',
      buttonIcon: 'info',
      caption: resources.messages['dataflowHelp'],
      handleRedirect: () => handleRedirect(getUrl(routes.DOCUMENTS, { dataflowId }, true)),
      helpClassName: 'dataflow-big-buttons-dataflowHelp-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.DOCUMENTS, { dataflowId }, true),
      visibility: true
    }
  ];

  const newSchemaModel = [
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
  ];

  const newSchemaBigButton = [
    {
      buttonClass: 'newItem',
      buttonIcon: 'plus',
      buttonIconClass: 'newItemCross',
      caption: resources.messages['newSchema'],
      handleRedirect: () => onShowNewSchemaDialog(),
      helpClassName: 'dataflow-new-schema-help-step',
      layout: buttonsVisibility.cloneSchemasFromDataflow ? 'menuBigButton' : 'defaultBigButton',
      model: buttonsVisibility.cloneSchemasFromDataflow ? newSchemaModel : [],
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
    enabled: buttonsVisibility.designDatasetsActions,
    handleRedirect: buttonsVisibility.designDatasetsActions
      ? () => {
          handleRedirect(getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true));
        }
      : () => {},

    helpClassName: 'dataflow-schema-help-step',
    index: newDatasetSchema.index,
    layout: 'defaultBigButton',
    model: buttonsVisibility.designDatasetsActions
      ? [
          {
            label: resources.messages['openDataset'],
            icon: 'openFolder',
            command: () => {
              handleRedirect(
                getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true)
              );
            }
          },
          {
            label: resources.messages['rename'],
            icon: 'pencil',
            disabled:
              dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'] || !buttonsVisibility.designDatasetsActions
          },
          {
            label: resources.messages['delete'],
            icon: 'trash',
            disabled:
              dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'] ||
              !buttonsVisibility.designDatasetsActions,
            command: () => getDeleteSchemaIndex(newDatasetSchema.index)
          }
          // {
          //   label: resources.messages['exportDatasetSchema'],
          //   icon: 'import',
          //   // disabled: dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'],
          //   command: () => exportDatatableSchema(newDatasetSchema.datasetId, newDatasetSchema.datasetSchemaName)
          // }
        ]
      : [],
    onSaveName: onSaveName,
    onWheel: getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true),
    placeholder: resources.messages['datasetSchemaNamePlaceholder'],
    setErrorDialogData: setErrorDialogData,
    tooltip: !buttonsVisibility.designDatasetsActions ? resources.messages['accessDenied'] : '',
    visibility:
      !isUndefined(dataflowState.data.designDatasets) &&
      isEmpty(dataflowState.data.dataCollections) &&
      buttonsVisibility.designDatasets &&
      dataflowState.status === DataflowConf.dataflowStatus['DESIGN']
  }));

  const buildGroupByRepresentativeModels = dataflowData => {
    const { datasets } = dataflowData;

    const allDatasets = datasets.map(dataset => {
      return {
        name: dataset.datasetSchemaName,
        id: dataset.dataProviderId,
        datasetId: dataset.datasetId,
        datasetName: dataset.name
      };
    });

    const isUniqRepresentative = uniq(allDatasets.map(dataset => dataset.id)).length === 1;

    if (isUniqRepresentative && !buttonsVisibility.groupByRepresentative) {
      return datasets.map(dataset => {
        const datasetName = dataset.name;
        const datasetId = dataset.datasetId;
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
          model: [
            {
              label: resources.messages['historicReleases'],
              command: () => {
                onShowHistoricReleases('reportingDataset');
                getDataHistoricReleases(datasetId, datasetName);
              }
            }
          ],
          onWheel: getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true),
          visibility: !isEmpty(dataflowState.data.datasets)
        };
      });
    }

    const datasetsIdsArray = [];
    const datasetsNamesArray = [];

    return allDatasets.map(representative => {
      datasetsIdsArray.push(representative.datasetId);
      datasetsNamesArray.push(representative.datasetName);

      return {
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
        model: [
          {
            label: resources.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset');
              getDataHistoricReleases(datasetsIdsArray, representative.name, datasetsNamesArray);
            }
          }
        ],
        onWheel: getUrl(routes.REPRESENTATIVE, { dataflowId, representativeId: representative.id }, true),
        visibility: !isEmpty(dataflowState.data.datasets)
      };
    });
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
      helpClassName: 'dataflow-create-datacollection-help-step',
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
      helpClassName: 'dataflow-updateNewRepresentatives-help-step',
      handleRedirect: isActiveButton ? () => onShowUpdateDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      visibility:
        buttonsVisibility.updateReporters &&
        dataflowState.status === 'DRAFT' &&
        dataflowState.hasRepresentativesWithoutDatasets
    }
  ];

  const dataCollectionModels = dataflowState.data.dataCollections.map(dataCollection => ({
    buttonClass: 'dataCollection',
    buttonIcon: 'dataCollection',
    caption: dataCollection.dataCollectionName,
    handleRedirect: () => {
      handleRedirect(getUrl(routes.DATA_COLLECTION, { dataflowId, datasetId: dataCollection.dataCollectionId }, true));
    },
    helpClassName: 'dataflow-datacollection-help-step',
    layout: 'defaultBigButton',
    model: [
      {
        label: resources.messages['historicReleases'],
        command: () => {
          onShowHistoricReleases('dataCollection');
          getDataHistoricReleases(dataCollection.dataCollectionId, dataCollection.dataCollectionName);
        }
      }
    ],
    visibility: !isEmpty(dataflowState.data.dataCollections)
  }));

  const euDatasetModels = dataflowState.data.euDatasets.map(euDataset => ({
    buttonClass: 'euDataset',
    buttonIcon: 'euDataset',
    caption: euDataset.euDatasetName,
    handleRedirect: () => {
      handleRedirect(getUrl(routes.EU_DATASET, { dataflowId, datasetId: euDataset.euDatasetId }, true));
    },
    helpClassName: 'dataflow-eudataset-help-step',
    layout: 'defaultBigButton',
    model: [
      {
        label: resources.messages['historicReleases'],
        command: () => {
          onShowHistoricReleases('EUDataset');
          getDataHistoricReleases(euDataset.euDatasetId, euDataset.euDatasetName);
        }
      }
    ],
    visibility: !isEmpty(dataflowState.data.euDatasets)
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
        helpClassName: 'dataflow-big-buttons-confirmation-receipt-help-step',
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
        handleRedirect:
          datasets.length > 1 ? () => {} : () => onShowSnapshotDialog(datasets[0].datasetId, datasets[0].name),
        helpClassName: 'dataflow-big-buttons-release-help-step',
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
          command: () => onShowSnapshotDialog(dataset.datasetId, dataset.name),
          disabled: false
        };
      });
    }

    return properties;
  };

  const copyDataCollectionToEuDatasetBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isCopyDataCollectionToEuDatasetLoading ? 'spinner' : 'angleDoubleRight',
      buttonIconClass: dataflowState.isCopyDataCollectionToEuDatasetLoading ? 'spinner' : '',
      caption: 'Copy Data Collections to EU Datasets',
      handleRedirect: dataflowState.isCopyDataCollectionToEuDatasetLoading
        ? () => {}
        : () => onShowCopyDataCollectionToEuDatasetModal(),
      layout: 'defaultBigButton',
      visibility:
        buttonsVisibility.copyDataCollectionToEuDataset && dataflowState.status === DataflowConf.dataflowStatus['DRAFT']
    }
  ];

  const exportEuDatasetModel = !isNil(dataflowState.data.euDatasets)
    ? [
        {
          label: resources.messages['updateConfigurations'],
          title: true
        }
      ].concat(
        dataflowState.data.euDatasets.map(dataset => ({
          command: () => {
            getDatasetData(dataset.euDatasetId, dataset.datasetSchemaId);
            handleExportEuDataset(true);
            onLoadEuDatasetIntegration(dataset.datasetSchemaId);
          },
          icon: 'export',
          iconStyle: { transform: 'rotate(-90deg)' },
          label: dataset.euDatasetName
        }))
      )
    : [];

  const exportEuDatasetBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isExportEuDatasetLoading ? 'spinner' : 'fileExport',
      buttonIconClass: dataflowState.isExportEuDatasetLoading ? 'spinner' : '',
      caption: 'Export EU Datasets',
      handleRedirect: dataflowState.isExportEuDatasetLoading ? () => {} : () => onShowExportEuDatasetModal(),
      layout: 'defaultBigButton',
      model: exportEuDatasetModel,
      visibility:
        buttonsVisibility.copyDataCollectionToEuDataset && dataflowState.status === DataflowConf.dataflowStatus['DRAFT']
    }
  ];

  const receiptBigButton = onBuildReceiptButton();

  const releaseBigButton = onBuildReleaseButton();

  return [
    ...manageReportersBigButton,
    ...helpBigButton,
    ...dashboardBigButton,
    ...dataCollectionModels,
    ...copyDataCollectionToEuDatasetBigButton,
    ...euDatasetModels,
    ...exportEuDatasetBigButton,
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
