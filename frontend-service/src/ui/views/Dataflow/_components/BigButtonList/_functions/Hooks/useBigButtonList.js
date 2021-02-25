import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import { config } from 'conf';
import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
  dataflowId,
  dataflowState,
  dataProviderId,
  getDataHistoricReleases,
  getDataHistoricReleasesByRepresentatives,
  getDatasetData,
  getDeleteSchemaIndex,
  handleExportEuDataset,
  handleRedirect,
  isActiveButton,
  isCloningDataflow,
  isImportingDataflow,
  isLeadReporterOfCountry,
  onCloneDataflow,
  onImportSchema,
  onLoadEuDatasetIntegration,
  onLoadReceiptData,
  onOpenReleaseConfirmDialog,
  onSaveName,
  onShowCopyDataCollectionToEuDatasetModal,
  onShowDataCollectionModal,
  onShowExportEuDatasetModal,
  onShowHistoricReleases,
  onShowManageReportersDialog,
  onShowManualTechnicalAcceptanceDialog,
  onShowNewSchemaDialog,
  onShowUpdateDataCollectionModal,
  setErrorDialogData
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  const isLeadDesigner = userContext.hasContextAccessPermission(config.permissions.DATAFLOW, dataflowId, [
    config.permissions.DATA_STEWARD,
    config.permissions.DATA_CUSTODIAN
  ]);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      setButtonsVisibility(getButtonsVisibility());
    }
  }, [userContext, dataflowState.data.datasets]);

  const getButtonsVisibility = () => {
    const isDesigner =
      isLeadDesigner ||
      userContext.hasContextAccessPermission(config.permissions.DATAFLOW, dataflowId, [
        config.permissions.EDITOR_WRITE
      ]);
    const isDesignStatus = dataflowState.status === DataflowConf.dataflowStatus['DESIGN'];
    const isDraftStatus = dataflowState.status === DataflowConf.dataflowStatus['OPEN'];
    const isManualAcceptance = dataflowState.data.manualAcceptance;
    const isReleased =
      !isNil(dataflowState.data.datasets) && dataflowState.data.datasets.some(dataset => dataset.isReleased);

    return {
      createDataCollection: isLeadDesigner && isDesignStatus,
      cloneSchemasFromDataflow: isLeadDesigner && isDesignStatus,
      copyDataCollectionToEuDataset: isLeadDesigner && isDraftStatus,
      exportEuDataset: isLeadDesigner && isDraftStatus,
      dashboard: isLeadDesigner && isDraftStatus,
      designDatasets:
        (isDesigner ||
          userContext.hasContextAccessPermission(config.permissions.DATAFLOW, dataflowId, [
            config.permissions.EDITOR_READ
          ])) &&
        isDesignStatus,
      designDatasetsActions: isDesigner && isDesignStatus,
      designDatasetsOpen: isLeadDesigner && isDraftStatus,
      feedback:
        (isLeadDesigner && isDraftStatus && isManualAcceptance) ||
        (isLeadReporterOfCountry && isReleased && isManualAcceptance),
      groupByRepresentative: isLeadDesigner && isDraftStatus,
      manageReporters: isLeadDesigner,
      manualTechnicalAcceptance: isLeadDesigner && isManualAcceptance,
      newSchema: isDesigner && isDesignStatus,
      updateDataCollection: isLeadDesigner && isDraftStatus,
      receipt: isLeadReporterOfCountry && isReleased,
      release: isLeadReporterOfCountry
    };
  };

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

  const feedbackBigButton = [
    {
      buttonClass: 'technicalFeedback',
      buttonIcon: 'comments',
      caption: resources.messages['technicalFeedback'],
      handleRedirect: () =>
        handleRedirect(
          !isLeadDesigner
            ? getUrl(routes.DATAFLOW_FEEDBACK, { dataflowId, representativeId: dataProviderId }, true)
            : getUrl(routes.DATAFLOW_FEEDBACK_CUSTODIAN, { dataflowId }, true)
        ),
      helpClassName: 'dataflow-big-buttons-technicalFeedback-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.DATAFLOW_FEEDBACK, { dataflowId }, true),
      visibility: buttonsVisibility.feedback
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
      icon: 'clone',
      command: () => onCloneDataflow()
    },
    {
      label: resources.messages['importSchema'],
      icon: 'import',
      command: () => onImportSchema()
    }
  ];

  const newSchemaBigButton = [
    {
      buttonClass: 'newItem',
      buttonIcon: isCloningDataflow || isImportingDataflow ? 'spinner' : 'plus',
      buttonIconClass: isCloningDataflow || isImportingDataflow ? 'spinner' : 'newItemCross',
      caption: resources.messages['newSchema'],
      handleRedirect: !isCloningDataflow && !isImportingDataflow ? () => onShowNewSchemaDialog() : () => {},
      helpClassName: 'dataflow-new-schema-help-step',
      layout:
        buttonsVisibility.cloneSchemasFromDataflow && !isCloningDataflow && !isImportingDataflow
          ? 'menuBigButton'
          : 'defaultBigButton',
      model:
        buttonsVisibility.cloneSchemasFromDataflow && !isCloningDataflow && !isImportingDataflow ? newSchemaModel : [],
      visibility: buttonsVisibility.newSchema
    }
  ];

  const designDatasetModels = isNil(dataflowState.data.designDatasets)
    ? []
    : dataflowState.data.designDatasets.map(newDatasetSchema => ({
        buttonClass: 'schemaDataset',
        buttonIcon: 'pencilRuler',
        canEditName: buttonsVisibility.designDatasetsActions,
        caption: newDatasetSchema.datasetSchemaName,
        dataflowStatus: dataflowState.status,
        datasetSchemaInfo: dataflowState.updatedDatasetSchema,
        enabled: buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetsOpen,
        handleRedirect:
          buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetsOpen
            ? () => {
                handleRedirect(
                  getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true)
                );
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
                  dataflowState.status !== DataflowConf.dataflowStatus['DESIGN'] ||
                  !buttonsVisibility.designDatasetsActions
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
        tooltip:
          !buttonsVisibility.designDatasetsActions && !buttonsVisibility.designDatasetsOpen
            ? resources.messages['accessDenied']
            : '',
        visibility: buttonsVisibility.designDatasets || buttonsVisibility.designDatasetsOpen
      }));

  const buildGroupByRepresentativeModels = (datasets = []) => {
    const allDatasets = datasets.map(dataset => {
      return {
        datasetId: dataset.datasetId,
        datasetName: dataset.name,
        dataProviderId: dataset.dataProviderId,
        name: dataset.datasetSchemaName
      };
    });

    const isUniqRepresentative = uniq(allDatasets.map(dataset => dataset.id)).length === 1;

    if (!buttonsVisibility.groupByRepresentative && isUniqRepresentative) {
      return allDatasets.map(dataset => {
        return {
          buttonClass: 'dataset',
          buttonIcon: 'dataset',
          caption: dataset.datasetName,
          helpClassName: 'dataflow-dataset-help-step',
          handleRedirect: () => {
            handleRedirect(getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true));
          },
          infoStatus: dataset.isReleased,
          infoStatusIcon: true,
          layout: 'defaultBigButton',
          model: [
            {
              label: resources.messages['historicReleases'],
              command: () => {
                onShowHistoricReleases('reportingDataset');
                getDataHistoricReleases(dataset.datasetId, dataset.datasetName);
              }
            }
          ],
          onWheel: getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true),
          visibility: true
        };
      });
    }

    return uniqBy(allDatasets, 'dataProviderId').map(dataset => {
      return {
        buttonClass: 'dataset',
        buttonIcon: 'representative',
        caption: dataset.name,
        handleRedirect: () => {
          handleRedirect(
            getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataset.dataProviderId }, true)
          );
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        layout: 'defaultBigButton',
        model: [
          {
            label: resources.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset');
              getDataHistoricReleasesByRepresentatives(dataset.name, dataset.dataProviderId);
            }
          }
        ],
        onWheel: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataset.dataProviderId }, true),
        visibility: true
      };
    });
  };

  const groupByRepresentativeModels = buildGroupByRepresentativeModels(dataflowState?.data?.datasets);

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
      visibility: buttonsVisibility.dashboard
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
      visibility: buttonsVisibility.createDataCollection
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
      visibility: buttonsVisibility.updateDataCollection && dataflowState.hasRepresentativesWithoutDatasets
    }
  ];

  const dataCollectionModels = isNil(dataflowState.data.dataCollections)
    ? []
    : dataflowState.data.dataCollections.map(dataCollection => ({
        buttonClass: 'dataCollection',
        buttonIcon: 'dataCollection',
        caption: dataCollection.dataCollectionName,
        handleRedirect: () => {
          handleRedirect(
            getUrl(routes.DATA_COLLECTION, { dataflowId, datasetId: dataCollection.dataCollectionId }, true)
          );
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
        visibility: true
      }));

  const euDatasetModels = isNil(dataflowState.data.euDatasets)
    ? []
    : dataflowState.data.euDatasets.map(euDataset => ({
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
        visibility: true
      }));

  const onBuildReceiptButton = () => {
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
        visibility: buttonsVisibility.receipt
      }
    ];
  };

  const isReleasing =
    !isNil(dataflowState.data.datasets) && dataflowState.data.datasets.length >= 1
      ? dataflowState.data.datasets[0].isReleasing
      : false;

  const onBuildReleaseButton = () => {
    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: isReleasing ? 'spinner' : 'released',
        buttonIconClass: isReleasing ? 'spinner' : 'released',
        caption: resources.messages['releaseDataCollection'],
        enabled: dataflowState.isReleasable,
        handleRedirect: dataflowState.isReleasable && !isReleasing ? () => onOpenReleaseConfirmDialog() : () => {},
        helpClassName: 'dataflow-big-buttons-release-help-step',
        layout: 'defaultBigButton',
        tooltip: dataflowState.isReleasable ? '' : resources.messages['releaseButtonTooltip'],
        visibility: buttonsVisibility.release
      }
    ];
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
      visibility: buttonsVisibility.copyDataCollectionToEuDataset
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
      visibility: buttonsVisibility.exportEuDataset
    }
  ];

  const manualTechnicalAcceptanceBigButton = [
    {
      buttonClass: 'manualTechnicalAcceptance',
      buttonIcon: 'reply',
      caption: resources.messages['manualTechnicalAcceptanceBigButton'],
      handleRedirect: () => onShowManualTechnicalAcceptanceDialog(),
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.manualTechnicalAcceptance
    }
  ];

  const receiptBigButton = onBuildReceiptButton();

  const releaseBigButton = onBuildReleaseButton();

  return [
    ...manageReportersBigButton,
    ...helpBigButton,
    ...designDatasetModels,
    ...feedbackBigButton,
    ...dashboardBigButton,
    ...dataCollectionModels,
    ...manualTechnicalAcceptanceBigButton,
    ...copyDataCollectionToEuDatasetBigButton,
    ...euDatasetModels,
    ...exportEuDatasetBigButton,
    ...newSchemaBigButton,
    ...createDataCollection,
    ...updateDatasetsNewRepresentatives,
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...releaseBigButton
  ];
};

export { useBigButtonList };
