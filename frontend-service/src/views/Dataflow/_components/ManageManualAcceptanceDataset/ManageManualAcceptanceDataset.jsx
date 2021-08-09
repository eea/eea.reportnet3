import { Fragment, useContext, useReducer } from 'react';
import ReactTooltip from 'react-tooltip';

import camelCase from 'lodash/camelCase';
import isEmpty from 'lodash/isEmpty';
import uniqueId from 'lodash/uniqueId';

import styles from './ManageManualAcceptanceDataset.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { InputTextarea } from 'views/_components/InputTextarea';
import { RadioButton } from 'views/_components/RadioButton';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { DatasetService } from 'services/DatasetService';

import { manageManualAcceptanceDatasetReducer } from './_functions/Reducers/manageManualAcceptanceDatasetReducer';

export const ManageManualAcceptanceDataset = ({
  dataflowId,
  dataset,
  isManageManualAcceptanceDatasetDialogVisible,
  manageDialogs,
  refreshManualAcceptanceDatasets
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [manageManualAcceptanceDatasetState, manageManualAcceptanceDatasetDispatch] = useReducer(
    manageManualAcceptanceDatasetReducer,
    {
      initialDatasetMessage: '',
      datasetMessage: '',
      datasetFeedbackStatus: dataset.feedbackStatus
    }
  );

  const { datasetId, datasetName } = dataset;

  const onChangeMessage = message => {
    if (message !== manageManualAcceptanceDatasetState.initialDatasetMessage) {
      return manageManualAcceptanceDatasetDispatch({
        type: 'ON_UPDATE_MESSAGE',
        payload: { message }
      });
    }
  };

  const onChangeStatus = value =>
    manageManualAcceptanceDatasetDispatch({ type: 'ON_CHANGE_STATUS', payload: { value } });

  const onKeyChange = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      const value = event.target.value;
      manageManualAcceptanceDatasetDispatch({ type: 'ON_UPDATE_MESSAGE', payload: { value } });
      !isEmpty(manageManualAcceptanceDatasetState.datasetMessage) &&
        !isEmpty(manageManualAcceptanceDatasetState.datasetFeedbackStatus) &&
        onUpdateDataset();
    }
  };

  const onUpdateDataset = async () => {
    try {
      await DatasetService.updateDatasetFeedbackStatus(
        dataflowId,
        datasetId,
        manageManualAcceptanceDatasetState.datasetMessage,
        manageManualAcceptanceDatasetState.datasetFeedbackStatus
      );
      refreshManualAcceptanceDatasets(true);
    } catch (error) {
      console.error('ManageManualAcceptanceDataset - onUpdateDataset.', error);
      notificationContext.add({ type: 'UPDATE_DATASET_FEEDBACK_STATUS_ERROR' });
    } finally {
      manageDialogs(false);
    }
  };

  const renderDialogFooter = (
    <Fragment>
      <span data-for="createTooltip" data-tip>
        <Button
          className={`p-button-primary ${
            !isEmpty(manageManualAcceptanceDatasetState.datasetMessage) &&
            manageManualAcceptanceDatasetState.datasetFeedbackStatus !== dataset.feedbackStatus
              ? 'p-button-animated-blink'
              : ''
          }`}
          disabled={
            isEmpty(manageManualAcceptanceDatasetState.datasetMessage) ||
            manageManualAcceptanceDatasetState.datasetFeedbackStatus === dataset.feedbackStatus
          }
          icon={'check'}
          label={resources.messages['update']}
          onClick={() => onUpdateDataset()}
        />
      </span>
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs(false)}
      />
      {(isEmpty(manageManualAcceptanceDatasetState.datasetMessage) ||
        manageManualAcceptanceDatasetState.datasetFeedbackStatus === dataset.feedbackStatus) && (
        <ReactTooltip border={true} className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
          <span>{resources.messages['fcSubmitButtonDisabled']}</span>
        </ReactTooltip>
      )}
    </Fragment>
  );
  const renderDialogLayout = children => (
    <Dialog
      footer={renderDialogFooter}
      header={`${resources.messages['editStatusDataset']} ${datasetName}`}
      onHide={() => manageDialogs(false)}
      style={{ width: '50%' }}
      visible={isManageManualAcceptanceDatasetDialogVisible}>
      {children}
    </Dialog>
  );

  const idTextArea = uniqueId();

  const renderInputTextLayout = option => (
    <span>
      <label htmlFor={idTextArea}>{resources.messages[option]}</label>
      <InputTextarea
        className={`${styles.datasetMessage} datasetSchema-metadata-help-step`}
        collapsedHeight={85}
        id={idTextArea}
        onChange={e => onChangeMessage(e.target.value)}
        onFocus={e =>
          manageManualAcceptanceDatasetDispatch({
            type: 'INITIAL_DATASET_MESSAGE',
            payload: { value: e.target.value }
          })
        }
        onKeyDown={e => onKeyChange(e)}
        placeholder={resources.messages['feedbackMessagePlaceholder']}
      />
    </span>
  );

  const renderRadioButtons = () =>
    ['Technically accepted', 'Correction requested'].map(feedbackStatus => {
      return (
        <div className={styles.radioButtonWrapper} key={feedbackStatus}>
          <RadioButton
            checked={manageManualAcceptanceDatasetState.datasetFeedbackStatus === feedbackStatus}
            className={styles.radioButton}
            inputId={feedbackStatus}
            onChange={event => onChangeStatus(event.target.value)}
            value={feedbackStatus}
          />
          <label className={styles.label} htmlFor={feedbackStatus}>
            {resources.messages[camelCase(feedbackStatus)]}
          </label>
        </div>
      );
    });

  return renderDialogLayout(
    <div className={styles.content}>
      <div className={styles.group}>
        <div>{resources.messages['feedbackStatus']}</div>
        {renderRadioButtons()}
      </div>
      <div className={styles.group}>{renderInputTextLayout('feedbackMessage')}</div>
    </div>
  );
};
