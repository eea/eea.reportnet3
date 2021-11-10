import { useContext, useLayoutEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { routes } from 'conf/routes';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const useBigButtonListRepresentative = ({
  dataflowState,
  dataProviderId,
  getDataHistoricReleases,
  handleRedirect,
  isLeadReporterOfCountry,
  match,
  onLoadReceiptData,
  onOpenReleaseConfirmDialog,
  onShowHistoricReleases
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
    const isTestDataset = parseInt(match.params.representativeId) === 0;
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
      testDatasets: isTestDataset
    };
  };

  const feedbackButton = {
    layout: 'defaultBigButton',
    buttonClass: 'technicalFeedback',
    buttonIcon: 'comments',
    caption: resourcesContext.messages['technicalFeedback'],
    handleRedirect: () =>
      handleRedirect(
        getUrl(
          routes.DATAFLOW_FEEDBACK,
          {
            dataflowId: dataflowState.id,
            representativeId: dataProviderId
          },
          true
        )
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
    handleRedirect: () =>
      handleRedirect(
        getUrl(
          routes.DOCUMENTS,
          {
            dataflowId: dataflowState.id
          },
          true
        )
      ),
    helpClassName: 'dataflow-documents-webLinks-help-step',
    onWheel: getUrl(
      routes.DOCUMENTS,
      {
        dataflowId: dataflowState.id
      },
      true
    ),
    visibility: buttonsVisibility.help
  };

  const testDatasetsModels = dataflowState.data.testDatasets?.map(testDataset => {
    return {
      layout: 'defaultBigButton',
      buttonClass: 'dataset',
      buttonIcon: 'dataset',
      caption: testDataset.datasetSchemaName,
      infoStatus: testDataset.isReleased,
      infoStatusIcon: testDataset.isReleased,
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
    .filter(dataset => dataset.dataProviderId === parseInt(match.params.representativeId))
    .map(dataset => {
      const datasetName = dataset.name;
      const datasetId = dataset.datasetId;
      return {
        layout: 'defaultBigButton',
        buttonClass: 'dataset',
        buttonIcon: 'dataset',
        caption: dataset.name,
        handleRedirect: () => {
          handleRedirect(getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true));
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        model: [
          {
            label: resourcesContext.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset', true);
              getDataHistoricReleases(datasetId, datasetName);
            }
          }
        ],
        onWheel: getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true),
        visibility: true
      };
    });

  const onBuildReceiptButton = () => {
    return [
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
  };

  const receiptBigButton = onBuildReceiptButton();

  const getIsReleasing = () => {
    return dataflowState?.data?.datasets?.some(
      dataset => dataset.isReleasing && dataset.dataProviderId === dataProviderId
    );
  };

  const isReleased = dataflowState.data.datasets
    .filter(dataset => dataset.dataProviderId === parseInt(match.params.representativeId))
    .some(dataset => dataset.isReleased);

  const representative = dataflowState.data.representatives.find(
    representative => representative.dataProviderId === dataProviderId
  );

  const onBuildReleaseButton = () => {
    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: getIsReleasing() ? 'spinner' : 'released',
        buttonIconClass: getIsReleasing() ? 'spinner' : 'released',
        caption: resourcesContext.messages['releaseDataCollection'],
        enabled: dataflowState.isReleasable && !getIsReleasing(),
        handleRedirect: dataflowState.isReleasable && !getIsReleasing() ? () => onOpenReleaseConfirmDialog() : () => {},
        helpClassName: 'dataflow-big-buttons-release-help-step',
        infoStatus: isReleased,
        infoStatusIcon: isReleased,
        restrictFromPublicIsUpdating: dataflowState.restrictFromPublicIsUpdating.value,
        layout: 'defaultBigButton',
        restrictFromPublicAccess:
          isLeadReporterOfCountry && !TextUtils.areEquals(dataflowState.status, 'business') && !getIsReleasing(),
        restrictFromPublicInfo: dataflowState.data.showPublicInfo && isReleased,
        restrictFromPublicStatus: representative.restrictFromPublic,
        tooltip: dataflowState.isReleasable ? '' : resourcesContext.messages['releaseButtonTooltip'],
        visibility: buttonsVisibility.release
      }
    ];
  };

  const releaseBigButton = onBuildReleaseButton();

  return [
    helpButton,
    feedbackButton,
    ...groupByRepresentativeModels,
    ...receiptBigButton,
    ...releaseBigButton,
    ...testDatasetsModels
  ];
};

export { useBigButtonListRepresentative };
