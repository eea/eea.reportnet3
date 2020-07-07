import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';

const useBigButtonList = ({ handleRedirect, onLoadReceiptData, dataflowState, onShowSnapshotDialog, match }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [buttonsVisibility, setButtonsVisibility] = useState({});

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.DATAFLOW}${dataflowState.id}`);
      setButtonsVisibility(getButtonsVisibility(userRoles.map(userRole => config.permissions[userRole])));
      // setButtonsVisibility(
      //   getButtonsVisibility(['LEAD_REPORTER', 'REPORTER_READ'].map(userRole => config.permissions[userRole]))
      // );
    }
  }, [userContext]);

  const getButtonsVisibility = roles => ({
    receipt: roles.includes(config.permissions['LEAD_REPORTER']) || roles.includes(config.permissions['REPORTER']),
    release:
      roles.includes(config.permissions['LEAD_REPORTER']) &&
      !roles.includes(config.permissions['REPORTER_WRITE']) &&
      !roles.includes(config.permissions['REPORTER_READ'])
  });

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
        onWheel: getUrl(
          routes.DATASET,
          {
            dataflowId: dataflowState.id,
            datasetId: dataset.datasetId
          },
          true
        ),
        // model: !dataflowState.hasWritePermissions && [
        //   {
        //     label: resources.messages['properties'],
        //     icon: 'info',
        //     disabled: true
        //   }
        // ],
        visibility: !isEmpty(dataflowState.data.datasets)
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

  const onBuildReleaseButton = () => {
    const { datasets } = dataflowState.data;

    const filteredDatasets = datasets.filter(
      dataset => dataset.dataProviderId === parseInt(match.params.representativeId)
    );

    const properties = [
      {
        buttonClass: 'schemaDataset',
        buttonIcon: 'released',
        buttonIconClass: 'released',
        caption: resources.messages['releaseDataCollection'],
        handleRedirect:
          filteredDatasets.length > 1 ? () => {} : () => onShowSnapshotDialog(filteredDatasets[0].datasetId),
        layout: filteredDatasets.length > 1 ? 'menuBigButton' : 'defaultBigButton',
        visibility:
          buttonsVisibility.release && dataflowState.status !== 'DESIGN' && !isEmpty(dataflowState.data.datasets)
      }
    ];

    if (filteredDatasets.length > 1) {
      properties[0].model = filteredDatasets.map(dataset => {
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

  return [helpButton, ...groupByRepresentativeModels, ...receiptBigButton, ...releaseBigButton];
};

export { useBigButtonList };
