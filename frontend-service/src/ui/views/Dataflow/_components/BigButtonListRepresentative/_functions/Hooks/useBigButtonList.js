import { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import { routes } from 'ui/routes';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({
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
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      setButtonsVisibility(getButtonsVisibility());
    }
  }, [userContext, dataflowState.data.datasets]);

  const getButtonsVisibility = () => {
    const isManualAcceptance = dataflowState.data.manualAcceptance;
    const isTestDataset = match.params.representativeId === 'XX';
    const isReleased =
      !isNil(dataflowState.data.datasets) &&
      dataflowState.data.datasets.some(dataset => dataset.isReleased && dataset.dataProviderId === dataProviderId);

    return {
      feedback: isLeadReporterOfCountry && isReleased && isManualAcceptance,
      help: !isTestDataset,
      receipt: isLeadReporterOfCountry && isReleased,
      release: isLeadReporterOfCountry,
      testDatasets: isTestDataset
    };
  };

  const feedbackButton = {
    layout: 'defaultBigButton',
    buttonClass: 'technicalFeedback',
    buttonIcon: 'comments',
    caption: resources.messages['technicalFeedback'],
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
    caption: resources.messages['dataflowHelp'],
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
    helpClassName: 'dataflow-documents-weblinks-help-step',
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
      visibility: true
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
        infoStatus: dataset.isReleased,
        infoStatusIcon: dataset.isReleased,
        handleRedirect: () => {
          handleRedirect(getUrl(routes.DATASET, { dataflowId: dataflowState.id, datasetId: dataset.datasetId }, true));
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        model: [
          {
            label: resources.messages['historicReleases'],
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
        caption: resources.messages['confirmationReceipt'],
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

  const onBuildReleaseButton = () => {
    return [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: getIsReleasing() ? 'spinner' : 'released',
        buttonIconClass: getIsReleasing() ? 'spinner' : 'released',
        caption: resources.messages['releaseDataCollection'],
        enabled: dataflowState.isReleasable,
        handleRedirect: dataflowState.isReleasable && !getIsReleasing() ? () => onOpenReleaseConfirmDialog() : () => {},
        helpClassName: 'dataflow-big-buttons-release-help-step',
        layout: 'defaultBigButton',
        tooltip: dataflowState.isReleasable ? '' : resources.messages['releaseButtonTooltip'],
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

export { useBigButtonList };
