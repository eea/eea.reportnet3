import { useContext, useLayoutEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const useBigButtonListRepresentative = ({
  bigData,
  code,
  dataflowId,
  dataflowState,
  dataProviderId,
  getDataHistoricReleases,
  handleRedirect,
  hasActiveLocks,
  isAdmin,
  isCreatingPreparationSets,
  isLeadReporterOfCountry,
  onCreatePreparationSets,
  onLoadReceiptData,
  onOpenReleaseConfirmDialog,
  onOpenSilentReleaseConfirmDialog,
  onShowHistoricReleases,
  representativeId,
  onShowReleaseSnapshots,
  getDataReleaseSnapshots,
  onShowManagePreparationSetsDialog,
  preparationSetsList,
  setSelectedPreparationSet
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  const notCreatedSets = preparationSetsList?.filter(set => set.isCreated === false);

  useLayoutEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      setButtonsVisibility(getButtonsVisibility());
    }
  }, [userContext, dataflowState.data.datasets]);

  const getButtonsVisibility = () => {
    const isManualAcceptance = dataflowState.data.manualAcceptance;
    const isTestDataset = parseInt(representativeId) === 0;
    const isStewardSupport = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
      config.permissions.roles.STEWARD_SUPPORT.key
    ]);
    const isReleased =
      !isNil(dataflowState.data.datasets) &&
      dataflowState.data.datasets.some(dataset => dataset.isReleased && dataset.dataProviderId === dataProviderId);

    const representativeWithSameDataProviderID = dataflowState.data?.representatives?.find(
      representative => representative.dataProviderId === dataProviderId
    );

    const isLeadReporterOfThisCountry = !isEmpty(representativeWithSameDataProviderID)
      ? representativeWithSameDataProviderID.leadReporters?.some(
          leadReporter => leadReporter.account === userContext.email
        )
      : false;

    return {
      createPreparationSets: isLeadReporterOfThisCountry,
      feedback: isLeadReporterOfThisCountry && isReleased && isManualAcceptance,
      help: true,
      managePreparationSets: isLeadReporterOfThisCountry,
      receipt: isLeadReporterOfThisCountry && isReleased,
      release: isLeadReporterOfThisCountry && !isTestDataset,
      silentRelease: !isTestDataset && isAdmin,
      testDatasets: isTestDataset || (isStewardSupport && isTestDataset)
    };
  };

  const getReferenceDatasetModels = () => {
    if (
      isNil(dataflowState.data.referenceDatasets) ||
      dataflowState.data.representatives.length > 1 ||
      dataflowState.hasCustodianPermissions ||
      code
    ) {
      return [];
    }

    return dataflowState.data.referenceDatasets.map(referenceDataset => ({
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
      onWheel: getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: referenceDataset.datasetId }, true),
      visibility: true
    }));
  };

  const managePreparationSetsBigButton = [
    {
      buttonClass: 'managePreparationSets',
      buttonIcon: 'managePreparationSets',
      caption: resourcesContext.messages['managePreparationSets'],
      handleRedirect: () => onShowManagePreparationSetsDialog(true),
      layout: 'defaultBigButton',
      visibility: bigData && !code && buttonsVisibility.managePreparationSets
    }
  ];

  const feedbackButton = {
    layout: 'defaultBigButton',
    buttonClass: 'technicalFeedback',
    buttonIcon: 'comments',
    caption: resourcesContext.messages['technicalFeedback'],
    handleRedirect: () =>
      handleRedirect(
        getUrl(routes.DATAFLOW_FEEDBACK, { dataflowId: dataflowState.id, representativeId: dataProviderId }, true)
      ),
    helpClassName: 'dataflow-feedback-help-step',
    onWheel: getUrl(
      routes.DATAFLOW_FEEDBACK,
      {
        dataflowId: dataflowState.id,
        representativeId: dataProviderId
      },
      true
    ),
    visibility: !code && buttonsVisibility.feedback
  };

  const helpButton = {
    layout: 'defaultBigButton',
    buttonClass: 'dataflowHelp',
    buttonIcon: 'info',
    caption: resourcesContext.messages['dataflowHelp'],
    handleRedirect: () => handleRedirect(getUrl(routes.DOCUMENTS, { dataflowId: dataflowState.id }, true)),
    helpClassName: 'dataflow-documents-webLinks-help-step',
    onWheel: getUrl(routes.DOCUMENTS, { dataflowId: dataflowState.id }, true),
    visibility: !code && buttonsVisibility.help
  };

  const testDatasetsModels = (dataflowState.data?.testDatasets ?? []).map(testDataset => {
    return {
      layout: 'defaultBigButton',
      buttonClass: 'dataset',
      buttonIcon: 'dataset',
      caption: testDataset.datasetSchemaName,
      infoStatus: false,
      infoStatusIcon: false,
      handleRedirect: () => {
        handleRedirect(
          getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: testDataset.datasetId }, true)
        );
      },
      helpClassName: 'dataflow-dataset-container-help-step',
      model: [],
      onWheel: getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: testDataset.datasetId }, true),
      visibility: buttonsVisibility.testDatasets
    };
  });

  const groupByRepresentativeModels = (dataflowState.data?.datasets ?? [])
    .filter(dataset => dataset.dataProviderId === parseInt(representativeId))
    .map(dataset => {
      const getTechnicalAcceptanceStatus = () => {
        if (!dataflowState.data.manualAcceptance) {
          return null;
        }

        return resourcesContext.messages[config.datasetStatus[dataset.status].label];
      };

      const technicalAcceptanceStatus = getTechnicalAcceptanceStatus();

      return {
        layout: 'defaultBigButton',
        buttonClass: 'dataset',
        buttonIcon: 'dataset',
        caption: dataset.name,
        handleRedirect: () => {
          handleRedirect(
            code
              ? getUrl(
                  routes.PREPARATION_DATASET,
                  { dataflowId: dataflowState.id, datasetId: dataset.datasetId, code },
                  true
                )
              : getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true)
          );
        },
        hasUpdatesAfterRelease: dataset.hasUpdatesAfterRelease,
        hasUpdatesAfterReleaseTooltip:
          dataset.hasUpdatesAfterRelease && resourcesContext.messages['hasUpdatesAfterReleaseDatasetTooltip'],
        helpClassName: 'dataflow-dataset-container-help-step',
        infoStatus: dataset.isReleased,
        infoStatusIcon: true,
        model:
          dataflowState.id === '557' || dataflowState.id === '615'
            ? [
                {
                  label: resourcesContext.messages['historicReleases'],
                  command: () => {
                    onShowHistoricReleases('reportingDataset', true);
                    getDataHistoricReleases(dataset.datasetId, dataset.name);
                  }
                },
                {
                  label: resourcesContext.messages['releaseSnapshots'],
                  command: () => {
                    onShowReleaseSnapshots('reportingDataset', true);
                    getDataReleaseSnapshots(dataset.datasetId, dataset.name);
                  }
                }
              ]
            : !code && [
                {
                  label: resourcesContext.messages['historicReleases'],
                  command: () => {
                    onShowHistoricReleases('reportingDataset', true);
                    getDataHistoricReleases(dataset.datasetId, dataset.name);
                  }
                }
              ],
        onWheel: code
          ? getUrl(
              routes.PREPARATION_DATASET,
              { dataflowId: dataflowState.id, datasetId: dataset.datasetId, code },
              true
            )
          : getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true),
        technicalAcceptanceStatus: technicalAcceptanceStatus,
        visibility: true
      };
    })
    .sort((a, b) => a.caption.localeCompare(b.caption));

  const onBuildReceiptButton = () => [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      buttonIconClass: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      caption: resourcesContext.messages['confirmationReceipt'],
      handleRedirect: dataflowState.isReceiptLoading ? () => {} : () => onLoadReceiptData(),
      infoStatus: dataflowState.isReceiptOutdated,
      layout: 'defaultBigButton',
      visibility: !code && buttonsVisibility.receipt
    }
  ];

  const receiptBigButton = onBuildReceiptButton();

  const buildPreparationSetsModels = () => {
    return preparationSetsList?.map(set => {
      return {
        buttonClass: 'preparationSet',
        buttonIcon: 'preparationSet',
        caption: set.datasetName,
        helpClassName: 'dataflow-dataset-help-step',
        handleRedirect: () => {
          setSelectedPreparationSet(set);
          handleRedirect(
            getUrl(
              routes.PREPARATION_DATAFLOW_REPRESENTATIVE,
              { dataflowId: dataflowState.id, representativeId, code: set.code },
              true
            )
          );
        },
        layout: 'defaultBigButton',
        onWheel: getUrl(
          routes.PREPARATION_DATAFLOW_REPRESENTATIVE,
          { dataflowId: dataflowState.id, representativeId, code: set.code },
          true
        ),
        visibility: !code && set.isCreated
      };
    });
  };

  const createPreparationSets = [
    {
      buttonClass: 'newItem',
      buttonIcon: hasActiveLocks || isCreatingPreparationSets ? 'spinner' : 'createPreparationSets',
      buttonIconClass: hasActiveLocks || isCreatingPreparationSets ? 'spinner' : '',
      caption: resourcesContext.messages['createPreparationSets'],
      enabled: !hasActiveLocks && !isEmpty(notCreatedSets) && !dataflowState.hasEnableEditingDatasets,
      handleRedirect: () =>
        !hasActiveLocks &&
        !isEmpty(notCreatedSets) &&
        !dataflowState.hasEnableEditingDatasets &&
        onCreatePreparationSets(),
      layout: 'defaultBigButton',
      tooltip:
        isEmpty(notCreatedSets) && preparationSetsList?.length > 0
          ? resourcesContext.messages['preparationSetsCreated']
          : !isEmpty(notCreatedSets) && dataflowState.hasEnableEditingDatasets
          ? resourcesContext.messages['createPreparationSetsDisableEditTooltip']
          : undefined,

      visibility: bigData && !code && buttonsVisibility.createPreparationSets
    }
  ];

  const preparationSetsModels = isEmpty(preparationSetsList) ? [] : buildPreparationSetsModels();

  const getIsReleasing = () =>
    dataflowState?.data?.datasets?.some(dataset => dataset.isReleasing && dataset.dataProviderId === dataProviderId);

  const isReleased = (dataflowState?.data?.datasets ?? [])
    .filter(dataset => dataset.dataProviderId === parseInt(representativeId))
    .some(dataset => dataset.isReleased);

  const representative = dataflowState?.data?.representatives?.find(
    representative => representative.dataProviderId === dataProviderId
  );

  const onBuildReleaseButton = isSilent => [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: getIsReleasing() ? 'spinner' : 'released',
      buttonIconClass: getIsReleasing() ? 'spinner' : 'released',
      caption: resourcesContext.messages[isSilent ? 'releaseDataCollectionSilently' : 'releaseDataCollection'],
      enabled: !dataflowState.hasEnableEditingDatasets && dataflowState.isReleasable && !getIsReleasing(),
      handleRedirect:
        !dataflowState.hasEnableEditingDatasets && dataflowState.isReleasable && !getIsReleasing()
          ? isSilent
            ? () => onOpenSilentReleaseConfirmDialog()
            : () => onOpenReleaseConfirmDialog()
          : () => {},
      helpClassName: 'dataflow-big-buttons-release-help-step',
      infoStatus: isReleased,
      infoStatusIcon: true,
      layout: 'defaultBigButton',
      restrictFromPublicAccess:
        isLeadReporterOfCountry && !TextUtils.areEquals(dataflowState.status, 'business') && !getIsReleasing(),
      restrictFromPublicInfo: dataflowState.data.showPublicInfo && isReleased,
      restrictFromPublicIsUpdating: dataflowState.restrictFromPublicIsUpdating.value,
      restrictFromPublicStatus: representative?.restrictFromPublic,
      tooltip: dataflowState.isReleasable ? '' : resourcesContext.messages['releaseButtonTooltip'],
      visibility: !code && (isSilent ? buttonsVisibility.silentRelease : buttonsVisibility.release)
    }
  ];

  const releaseBigButton = onBuildReleaseButton(false);
  const silentReleaseButton = onBuildReleaseButton(true);

  return [
    helpButton,
    feedbackButton,
    ...getReferenceDatasetModels(),
    ...groupByRepresentativeModels,
    ...testDatasetsModels,
    ...managePreparationSetsBigButton,
    ...createPreparationSets,
    ...preparationSetsModels,
    ...receiptBigButton,
    ...releaseBigButton,
    ...(isAdmin ? silentReleaseButton : [])
  ];
};

export { useBigButtonListRepresentative };
