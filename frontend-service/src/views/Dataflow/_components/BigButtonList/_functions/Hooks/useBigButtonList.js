import { useCallback, useContext, useLayoutEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const useBigButtonList = ({
  dataflowId,
  dataflowState,
  dataProviderId,
  getDataHistoricReleases,
  getDataHistoricReleasesByRepresentatives,
  getDatasetData,
  getDeleteSchemaIndex,
  handleExportEUDataset,
  handleRedirect,
  isActiveButton,
  isCloningDataflow,
  isImportingDataflow,
  isLeadReporterOfCountry,
  onCloneDataflow,
  onImportSchema,
  onLoadEUDatasetIntegration,
  onLoadReceiptData,
  onOpenReleaseConfirmDialog,
  onSaveName,
  onShowCopyDataCollectionToEUDatasetModal,
  onShowDataCollectionModal,
  onShowExportEUDatasetModal,
  onShowHistoricReleases,
  onShowManageReportersDialog,
  onShowManualTechnicalAcceptanceDialog,
  onShowNewSchemaDialog,
  onShowUpdateDataCollectionModal,
  setErrorDialogData
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  const isLeadDesigner = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
    config.permissions.roles.CUSTODIAN.key,
    config.permissions.roles.STEWARD.key
  ]);

  const restrictFromPublicAccess = isLeadReporterOfCountry && !TextUtils.areEquals(dataflowState.status, 'business');

  const getButtonsVisibility = useCallback(() => {
    const isDesigner =
      isLeadDesigner ||
      userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
        config.permissions.roles.EDITOR_WRITE.key
      ]);
    const isObserver = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.OBSERVER.key
    ]);
    const isDesignStatus = dataflowState.status === config.dataflowStatus.DESIGN;
    const isDraftStatus = dataflowState.status === config.dataflowStatus.OPEN;
    const isEditorRead = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.EDITOR_READ.key
    ]);
    const isManualAcceptance = dataflowState.data.manualAcceptance;
    const isReleased =
      !isNil(dataflowState.data.datasets) && dataflowState.data.datasets.some(dataset => dataset.isReleased);

    return {
      createDataCollection: isLeadDesigner && isDesignStatus,
      cloneSchemasFromDataflow: isLeadDesigner && isDesignStatus,
      copyDataCollectionToEUDataset: isLeadDesigner && isDraftStatus,
      exportEUDataset: isLeadDesigner && isDraftStatus,
      dashboard: (isLeadDesigner || isObserver) && isDraftStatus,
      designDatasets:
        (isLeadDesigner ||
          userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
            config.permissions.roles.EDITOR_READ.key,
            config.permissions.roles.EDITOR_WRITE.key
          ])) &&
        isDesignStatus,
      designDatasetsActions: isDesigner && isDesignStatus,
      designDatasetsOpen: (isLeadDesigner && isDraftStatus) || (isEditorRead && isDesignStatus),
      designDatasetEditorReadActions: isEditorRead && isDesignStatus,
      feedback:
        (isLeadDesigner && isDraftStatus && isManualAcceptance) ||
        (isLeadReporterOfCountry && isReleased && isManualAcceptance),
      groupByRepresentative: (isLeadDesigner || isObserver) && isDraftStatus,
      manageReporters: isLeadDesigner,
      manualTechnicalAcceptance: isLeadDesigner && isManualAcceptance,
      newSchema: isDesigner && isDesignStatus,
      updateDataCollection: isLeadDesigner && isDraftStatus,
      receipt: isLeadReporterOfCountry && isReleased,
      release: isLeadReporterOfCountry,
      testDatasetVisibility: isLeadDesigner && isDraftStatus
    };
  }, [
    dataflowId,
    dataflowState.data.datasets,
    dataflowState.data.manualAcceptance,
    dataflowState.status,
    isLeadDesigner,
    isLeadReporterOfCountry,
    userContext
  ]);

  useLayoutEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      setButtonsVisibility(getButtonsVisibility());
    }
  }, [userContext, dataflowState.data.datasets, getButtonsVisibility]);

  const manageReportersBigButton = [
    {
      buttonClass: 'manageReporters',
      buttonIcon: 'manageReporters',
      caption: resourcesContext.messages['manageReporters'],
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
      caption: resourcesContext.messages['technicalFeedback'],
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
      caption: resourcesContext.messages['dataflowHelp'],
      handleRedirect: () => handleRedirect(getUrl(routes.DOCUMENTS, { dataflowId }, true)),
      helpClassName: 'dataflow-big-buttons-dataflowHelp-help-step',
      layout: 'defaultBigButton',
      onWheel: getUrl(routes.DOCUMENTS, { dataflowId }, true),
      visibility: true
    }
  ];

  const newSchemaModel = [
    {
      label: resourcesContext.messages['createNewEmptyDatasetSchema'],
      icon: 'add',
      command: () => onShowNewSchemaDialog()
    },
    {
      label: resourcesContext.messages['cloneSchemasFromDataflow'],
      icon: 'clone',
      command: () => onCloneDataflow()
    },
    {
      label: resourcesContext.messages['importSchema'],
      icon: 'import',
      command: () => onImportSchema()
    }
  ];

  const newSchemaBigButton = [
    {
      buttonClass: 'newItem',
      buttonIcon: isCloningDataflow || isImportingDataflow ? 'spinner' : 'plus',
      buttonIconClass: isCloningDataflow || isImportingDataflow ? 'spinner' : 'newItemCross',
      caption: resourcesContext.messages['newSchema'],
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
        model:
          buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetEditorReadActions
            ? [
                {
                  label: resourcesContext.messages['openDataset'],
                  icon: 'openFolder',
                  command: () => {
                    handleRedirect(
                      getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true)
                    );
                  }
                },
                {
                  label: resourcesContext.messages['rename'],
                  icon: 'pencil',
                  disabled:
                    dataflowState.status !== config.dataflowStatus.DESIGN || !buttonsVisibility.designDatasetsActions
                },
                {
                  label: resourcesContext.messages['delete'],
                  icon: 'trash',
                  command: () => getDeleteSchemaIndex(newDatasetSchema.index),
                  disabled:
                    dataflowState.status !== config.dataflowStatus.DESIGN || !buttonsVisibility.designDatasetsActions
                }
              ]
            : [],
        onSaveName: onSaveName,
        onWheel: getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true),
        placeholder: resourcesContext.messages['datasetSchemaNamePlaceholder'],
        setErrorDialogData: setErrorDialogData,
        tooltip:
          !buttonsVisibility.designDatasetsActions && !buttonsVisibility.designDatasetsOpen
            ? resourcesContext.messages['accessDenied']
            : '',
        visibility: buttonsVisibility.designDatasets || buttonsVisibility.designDatasetsOpen
      }));

  const buildGroupByRepresentativeModels = (datasets = []) => {
    const allDatasets = datasets.map(dataset => {
      return {
        datasetId: dataset.datasetId,
        datasetName: dataset.name,
        dataProviderId: dataset.dataProviderId,
        isReleased: isNil(dataset.isReleased) ? false : dataset.isReleased,
        name: dataset.datasetSchemaName
      };
    });

    const isUniqRepresentative = uniq(allDatasets.map(dataset => dataset.dataProviderId)).length === 1;

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
              label: resourcesContext.messages['historicReleases'],
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
      const datasetRepresentative = dataflowState.data.representatives.find(
        representative => representative.dataProviderId === dataset.dataProviderId
      );
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
        infoStatus: dataset.isReleased,
        infoStatusIcon: true,
        layout: 'defaultBigButton',
        model: [
          {
            label: resourcesContext.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset');
              getDataHistoricReleasesByRepresentatives(dataset.name, dataset.dataProviderId);
            }
          }
        ],
        onWheel: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataset.dataProviderId }, true),
        restrictFromPublicAccess: restrictFromPublicAccess,
        restrictFromPublicInfo: dataflowState.showPublicInfo && dataset.isReleased,
        restrictFromPublicStatus: datasetRepresentative?.restrictFromPublic,
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
      caption: resourcesContext.messages['dashboards'],
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
      caption: resourcesContext.messages['createDataCollection'],
      enabled: checkDisabledDataCollectionButton(),
      helpClassName: 'dataflow-create-datacollection-help-step',
      handleRedirect:
        isActiveButton && checkDisabledDataCollectionButton() ? () => onShowDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      tooltip: !isEmpty(dataflowState.data.dataCollections)
        ? resourcesContext.messages['disabledCreateDataCollectionSchemas']
        : !dataflowState.isDataSchemaCorrect
        ? resourcesContext.messages['disabledCreateDataCollectionSchemasWithError']
        : !dataflowState.formHasRepresentatives
        ? resourcesContext.messages['disabledCreateDataCollectionNoProviders']
        : undefined,
      visibility: buttonsVisibility.createDataCollection
    }
  ];

  const updateDatasetsNewRepresentatives = [
    {
      buttonClass: 'newItem',
      buttonIcon: isActiveButton ? 'siteMap' : 'spinner',
      buttonIconClass: isActiveButton ? 'siteMap' : 'spinner',
      caption: resourcesContext.messages['updateDataCollection'],
      helpClassName: 'dataflow-updateNewRepresentatives-help-step',
      handleRedirect: isActiveButton ? () => onShowUpdateDataCollectionModal() : () => {},
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.updateDataCollection && dataflowState.hasRepresentativesWithoutDatasets
    }
  ];

  const referenceDatasetModels = isNil(dataflowState.data.referenceDatasets)
    ? []
    : dataflowState.data.referenceDatasets.map(referenceDataset => {
        return {
          layout: 'defaultBigButton',
          buttonClass: 'referenceDataset',
          buttonIcon: 'howTo',
          caption: referenceDataset.datasetSchemaName,
          handleRedirect: () => {
            handleRedirect(
              getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: referenceDataset.datasetId }, true)
            );
          },
          helpClassName: 'dataflow-dataset-container-help-step',
          model: [],
          onWheel: getUrl(
            routes.DATASET,
            { dataflowId: dataflowState.id, datasetId: referenceDataset.datasetId },
            true
          ),
          visibility: true
        };
      });

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
            label: resourcesContext.messages['historicReleases'],
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
            label: resourcesContext.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('EUDataset');
              getDataHistoricReleases(euDataset.euDatasetId, euDataset.euDatasetName);
            }
          }
        ],
        visibility: true
      }));

  const isReleasing = dataflowState?.data?.datasets?.some(dataset => dataset.isReleasing);

  const onBuildReceiptButton = () => {
    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
        buttonIconClass: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
        caption: resourcesContext.messages['confirmationReceipt'],
        enabled: !isReleasing,
        handleRedirect: dataflowState.isReceiptLoading || isReleasing ? () => {} : () => onLoadReceiptData(),
        helpClassName: 'dataflow-big-buttons-confirmation-receipt-help-step',
        infoStatus: dataflowState.isReceiptOutdated,
        layout: 'defaultBigButton',
        visibility: buttonsVisibility.receipt
      }
    ];
  };

  const isReleased = dataflowState.data.datasets
    .filter(dataset => dataset.dataProviderId === dataProviderId)
    .some(dataset => dataset.isReleased);

  console.log('dataflowState.restrictFromPublic :>> ', dataflowState.restrictFromPublic);

  const onBuildReleaseButton = () => {
    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: isReleasing ? 'spinner' : 'released',
        buttonIconClass: isReleasing ? 'spinner' : 'released',
        caption: resourcesContext.messages['releaseDataCollection'],
        enabled: dataflowState.isReleasable,
        handleRedirect: dataflowState.isReleasable && !isReleasing ? () => onOpenReleaseConfirmDialog() : () => {},
        helpClassName: 'dataflow-big-buttons-release-help-step',
        layout: 'defaultBigButton',
        tooltip: dataflowState.isReleasable ? '' : resourcesContext.messages['releaseButtonTooltip'],
        restrictFromPublicAccess: restrictFromPublicAccess,
        restrictFromPublicInfo: dataflowState.showPublicInfo && isReleased,
        restrictFromPublicStatus: dataflowState.restrictFromPublic,
        visibility: buttonsVisibility.release
      }
    ];
  };

  const copyDataCollectionToEUDatasetBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isCopyDataCollectionToEUDatasetLoading ? 'spinner' : 'angleDoubleRight',
      buttonIconClass: dataflowState.isCopyDataCollectionToEUDatasetLoading ? 'spinner' : '',
      caption: 'Copy Data Collections to EU datasets',
      handleRedirect: dataflowState.isCopyDataCollectionToEUDatasetLoading
        ? () => {}
        : () => onShowCopyDataCollectionToEUDatasetModal(),
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.copyDataCollectionToEUDataset
    }
  ];

  const exportEUDatasetModel = !isNil(dataflowState.data.euDatasets)
    ? [
        {
          label: resourcesContext.messages['updateConfigurations'],
          title: true
        }
      ].concat(
        dataflowState.data.euDatasets.map(dataset => ({
          command: () => {
            getDatasetData(dataset.euDatasetId, dataset.datasetSchemaId);
            handleExportEUDataset(true);
            onLoadEUDatasetIntegration(dataset.datasetSchemaId);
          },
          icon: 'export',
          iconStyle: { transform: 'rotate(-90deg)' },
          label: dataset.euDatasetName
        }))
      )
    : [];

  const exportEUDatasetBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isExportEUDatasetLoading ? 'spinner' : 'fileExport',
      buttonIconClass: dataflowState.isExportEUDatasetLoading ? 'spinner' : '',
      caption: 'Export EU datasets',
      handleRedirect: dataflowState.isExportEUDatasetLoading ? () => {} : () => onShowExportEUDatasetModal(),
      layout: 'defaultBigButton',
      model: exportEUDatasetModel,
      visibility: buttonsVisibility.exportEUDataset
    }
  ];

  const manualTechnicalAcceptanceBigButton = [
    {
      buttonClass: 'manualTechnicalAcceptance',
      buttonIcon: 'reply',
      caption: resourcesContext.messages['manualTechnicalAcceptanceBigButton'],
      handleRedirect: () => onShowManualTechnicalAcceptanceDialog(),
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.manualTechnicalAcceptance
    }
  ];

  const testDatasetBigButton = [
    {
      buttonClass: 'dataCollection',
      buttonIcon: 'representative',
      caption: resourcesContext.messages['testDatasetBigButton'],
      handleRedirect: () => {
        handleRedirect(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: 0 }, true));
      },
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.testDatasetVisibility
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
    ...referenceDatasetModels,
    ...dataCollectionModels,
    ...manualTechnicalAcceptanceBigButton,
    ...copyDataCollectionToEUDatasetBigButton,
    ...euDatasetModels,
    ...exportEUDatasetBigButton,
    ...testDatasetBigButton,
    ...newSchemaBigButton,
    ...createDataCollection,
    ...updateDatasetsNewRepresentatives,
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...releaseBigButton
  ];
};

export { useBigButtonList };
