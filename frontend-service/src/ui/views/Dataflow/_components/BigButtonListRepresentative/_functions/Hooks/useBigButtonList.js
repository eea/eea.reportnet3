import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

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
  /*   onShowSnapshotDialog, */
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      setButtonsVisibility(getButtonsVisibility());
    }
  }, [userContext]);

  const getButtonsVisibility = () => ({
    feedback: false, //isLeadReporterOfCountry,
    receipt: isLeadReporterOfCountry,
    release: isLeadReporterOfCountry
  });

  const feedbackButton = {
    layout: 'defaultBigButton',
    buttonClass: 'dataflowFeedback',
    buttonIcon: 'comments',
    caption: resources.messages['dataflowFeedback'],
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
    visibility: true
  };

  const groupByRepresentativeModels = dataflowState.data.datasets
    .filter(dataset => dataset.dataProviderId === parseInt(match.params.representativeId))
    .map(dataset => {
      const datasetName = dataset.name;
      const datasetId = dataset.datasetId;
      const dataProviderId = dataset.dataProviderId;
      return {
        layout: 'defaultBigButton',
        buttonClass: 'dataset',
        buttonIcon: 'dataset',
        caption: dataset.name,
        infoStatus: dataset.isReleased,
        infoStatusIcon: dataset.isReleased,
        handleRedirect: () => {
          handleRedirect(
            getUrl(
              routes.DATASET,
              {
                dataflowId: dataflowState.id,
                datasetId: dataset.datasetId
              },
              true
            )
          );
        },
        helpClassName: 'dataflow-dataset-container-help-step',
        model: [
          {
            label: resources.messages['historicReleases'],
            command: () => {
              onShowHistoricReleases('reportingDataset', true);
              getDataHistoricReleases(datasetId, datasetName, dataProviderId);
            }
          }
        ],
        onWheel: getUrl(
          routes.DATASET,
          {
            dataflowId: dataflowState.id,
            datasetId: dataset.datasetId
          },
          true
        ),
        visibility: true
      };
    });

  const onBuildReceiptButton = () => {
    const { datasets } = dataflowState.data;
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
          !isUndefined(releasedStates) &&
          !releasedStates.includes(false) &&
          !releasedStates.includes(null)
      }
    ];
  };

  const receiptBigButton = onBuildReceiptButton();

  const releaseBigButton = [
    {
      buttonClass: 'schemaDataset',
      buttonIcon: 'released',
      caption: resources.messages['releaseDataCollection'],
      handleRedirect: () => onOpenReleaseConfirmDialog(),
      helpClassName: 'dataflow-big-buttons-release-help-step',
      layout: 'defaultBigButton',
      visibility: buttonsVisibility.release
    }
  ];

  return [helpButton, feedbackButton, ...groupByRepresentativeModels, ...receiptBigButton, ...releaseBigButton];
};

export { useBigButtonList };
