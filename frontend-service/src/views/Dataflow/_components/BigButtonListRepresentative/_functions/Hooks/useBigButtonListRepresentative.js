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
  dataflowId,
  dataflowState,
  dataProviderId,
  getDataHistoricReleases,
  handleRedirect,
  isLeadReporterOfCountry,
  onLoadReceiptData,
  onOpenReleaseConfirmDialog,
  onShowHistoricReleases,
  representativeId
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

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
      feedback: isLeadReporterOfThisCountry && isReleased && isManualAcceptance,
      help: true,
      receipt: isLeadReporterOfThisCountry && isReleased,
      release: isLeadReporterOfThisCountry && !isTestDataset,
      testDatasets: isTestDataset || (isStewardSupport && isTestDataset)
    };
  };

  const getReferenceDatasetModels = () => {
    if (
      isNil(dataflowState.data.referenceDatasets) ||
      dataflowState.data.representatives.length > 1 ||
      dataflowState.hasCustodianPermissions
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
    visibility: buttonsVisibility.feedback
  };

  const helpButton = {
    layout: 'defaultBigButton',
    buttonClass: 'dataflowHelp',
    buttonIcon: 'info',
    caption: resourcesContext.messages['dataflowHelp'],
    handleRedirect: () => handleRedirect(getUrl(routes.DOCUMENTS, { dataflowId: dataflowState.id }, true)),
    helpClassName: 'dataflow-documents-webLinks-help-step',
    onWheel: getUrl(routes.DOCUMENTS, { dataflowId: dataflowState.id }, true),
    visibility: buttonsVisibility.help
  };

  const testDatasetsModels = dataflowState.data.testDatasets?.map(testDataset => {
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

  const groupByRepresentativeModels = dataflowState.data.datasets
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
          handleRedirect(getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true));
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        infoStatus: dataset.isReleased,
        infoStatusIcon: true,
        model: [
          {
            label: resourcesContext.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset', true);
              getDataHistoricReleases(dataset.datasetId, dataset.name);
            }
          }
        ],
        onWheel: getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true),
        technicalAcceptanceStatus: technicalAcceptanceStatus,
        visibility: true
      };
    });

  const onBuildReceiptButton = () => [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      buttonIconClass: dataflowState.isReceiptLoading ? 'spinner' : 'fileDownload',
      caption: resourcesContext.messages['confirmationReceipt'],
      handleRedirect: dataflowState.isReceiptLoading ? () => {} : () => onLoadReceiptData(),
      infoStatus: dataflowState.isReceiptOutdated,
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.receipt
    }
  ];

  const receiptBigButton = onBuildReceiptButton();

  const getIsReleasing = () =>
    dataflowState?.data?.datasets?.some(dataset => dataset.isReleasing && dataset.dataProviderId === dataProviderId);

  const isReleased = dataflowState.data.datasets
    .filter(dataset => dataset.dataProviderId === parseInt(representativeId))
    .some(dataset => dataset.isReleased);

  const representative = dataflowState.data.representatives.find(
    representative => representative.dataProviderId === dataProviderId
  );

  const onBuildReleaseButton = () => [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: getIsReleasing() ? 'spinner' : 'released',
      buttonIconClass: getIsReleasing() ? 'spinner' : 'released',
      caption: resourcesContext.messages['releaseDataCollection'],
      enabled: dataflowState.isReleasable && !getIsReleasing(),
      handleRedirect: dataflowState.isReleasable && !getIsReleasing() ? () => onOpenReleaseConfirmDialog() : () => {},
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
      visibility: buttonsVisibility.release
    }
  ];

  const releaseBigButton = onBuildReleaseButton();

  return [
    helpButton,
    feedbackButton,
    ...getReferenceDatasetModels(),
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...releaseBigButton,
    ...testDatasetsModels
  ];
};

export { useBigButtonListRepresentative };
