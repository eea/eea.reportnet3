import { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { FeedbackConfig } from 'repositories/config/FeedbackConfig';
import { FeedbackReporterHelpConfig } from 'conf/help/feedback/reporter';
import { FeedbackRequesterHelpConfig } from 'conf/help/feedback/requester';

import styles from './Feedback.module.scss';

import { Button } from 'views/_components/Button';
import { CustomFileUpload } from 'views/_components/CustomFileUpload';
import { InputTextarea } from 'views/_components/InputTextarea';
import { ListBox } from 'views/DatasetDesigner/_components/ListBox';
import { ListMessages } from './_components/ListMessages';
import { MainLayout } from 'views/_components/Layout';
import { Title } from 'views/_components/Title';

import { DataflowService } from 'services/DataflowService';
import { FeedbackService } from 'services/FeedbackService';
import { RepresentativeService } from 'services/RepresentativeService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { feedbackReducer } from './_functions/Reducers/feedbackReducer';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const Feedback = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [feedbackState, dispatchFeedback] = useReducer(feedbackReducer, {
    currentPage: 0,
    dataflowName: '',
    dataProviders: [],
    draggedFiles: null,
    importFileDialogVisible: false,
    isAdmin: false,
    isBusinessDataflow: false,
    isCustodian: undefined,
    isDragging: false,
    isLoading: true,
    isSending: false,
    messageFirstLoad: false,
    messages: [],
    messageToSend: '',
    newMessageAdded: false,
    selectedDataProvider: {}
  });

  const {
    currentPage,
    dataflowName,
    dataProviders,
    draggedFiles,
    importFileDialogVisible,
    isBusinessDataflow,
    isAdmin,
    isCustodian,
    isDragging,
    isLoading,
    isSending,
    messages,
    messageToSend,
    newMessageAdded,
    selectedDataProvider
  } = feedbackState;

  useEffect(() => {
    onGetDataflowDetails();
    leftSideBarContext.removeModels();
  }, []);

  useEffect(() => {
    leftSideBarContext.addHelpSteps(
      isCustodian ? FeedbackRequesterHelpConfig : FeedbackReporterHelpConfig,
      'feedbackHelp'
    );
  }, [messages, isCustodian]);

  useEffect(() => {
    if (isCustodian) {
      onLoadDataProviders();
    }
  }, [isCustodian]);

  useEffect(() => {
    if (!isNil(isCustodian)) {
      if (isCustodian) {
        if (!isEmpty(selectedDataProvider)) {
          onGetInitialMessages(selectedDataProvider.dataProviderId);
        }
      } else {
        if (!isAdmin) onGetInitialMessages(representativeId);
      }
    }
  }, [selectedDataProvider, isCustodian, isAdmin]);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const isCustodian = userContext.hasContextAccessPermission(config.permissions.prefixes.DATAFLOW, dataflowId, [
        config.permissions.roles.CUSTODIAN.key,
        config.permissions.roles.STEWARD.key
      ]);

      const isAdmin = userContext.accessRole.some(role => role === config.permissions.roles.ADMIN.key);
      dispatchFeedback({ type: 'SET_PERMISSIONS', payload: { isCustodian, isAdmin } });
    }
  }, [userContext]);

  useEffect(() => {
    const textArea = document.querySelector(`.${styles.sendMessageTextarea}`);
    if (textArea && textArea.scrollHeight >= '48' && textArea.scrollHeight <= 100) {
      textArea.style.height = `${textArea.scrollHeight}px`;
      textArea.style.overflow = 'hidden';
    }
    if (textArea && textArea.scrollHeight > 100) {
      textArea.style.height = '100px';
      textArea.style.overflowY = 'scroll';
    }
    if (textArea && messageToSend === '') {
      textArea.style.height = '48px';
    }
  }, [messageToSend]);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_FEEDBACK, dataflowId, history, isBusinessDataflow, isLoading });

  const markMessagesAsRead = async data => {
    //mark unread messages as read
    if (data.unreadMessages.length > 0) {
      const unreadMessages = data.unreadMessages
        .filter(unreadMessage => (isCustodian ? unreadMessage.direction : !unreadMessage.direction))
        .map(unreadMessage => ({ id: unreadMessage.id, read: true }));

      if (!isEmpty(unreadMessages)) {
        await FeedbackService.markMessagesAsRead(dataflowId, unreadMessages);
      }
    }
  };

  const onFirstLoadMessages = loadState => {
    dispatchFeedback({ type: 'ON_UPDATE_MESSAGE_FIRST_LOAD', payload: loadState });
  };

  const onChangeDataProvider = value => {
    if (isNil(value)) {
      dispatchFeedback({ type: 'RESET_MESSAGES', payload: [] });
    } else {
      onFirstLoadMessages(true);
    }

    dispatchFeedback({ type: 'SET_SELECTED_DATAPROVIDER', payload: value });
  };

  const onGetDataflowDetails = async () => {
    try {
      const data = await DataflowService.getDetails(dataflowId);
      const name = data.name;
      const isBusinessDataflow = TextUtils.areEquals(data.type, config.dataflowType.BUSINESS);
      dispatchFeedback({ type: 'SET_DATAFLOW_DETAILS', payload: { name, isBusinessDataflow } });
    } catch (error) {
      console.error('Feedback - onGetDataflowName.', error);
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {}
      });
    } finally {
      dispatchFeedback({ type: 'SET_IS_LOADING', payload: false });
    }
  };

  const onGetMoreMessages = async () => {
    if ((isCustodian && isEmpty(selectedDataProvider)) || isLoading) return;
    const data = await onLoadMessages(
      isCustodian ? selectedDataProvider.dataProviderId : representativeId,
      currentPage
    );

    await markMessagesAsRead(data);

    dispatchFeedback({ type: 'ON_LOAD_MORE_MESSAGES', payload: data.messages });
  };

  const onGetInitialMessages = async dataProviderId => {
    const data = await onLoadMessages(dataProviderId, 0);
    await markMessagesAsRead(data);

    dispatchFeedback({ type: 'SET_MESSAGES', payload: data.messages });
  };

  const onDrop = event => {
    let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;
    dispatchFeedback({ type: 'SET_DRAGGED_FILES', payload: files });
    event.currentTarget.style.border = '';
    event.currentTarget.style.opacity = '';
  };

  const onDragLeave = event => {
    dispatchFeedback({ type: 'TOGGLE_IS_DRAGGING', payload: false });
    event.currentTarget.style.border = '';
    event.currentTarget.style.opacity = '';
    // event.currentTarget.innerText = '';
    event.preventDefault();
  };

  const onDragOver = event => {
    if (isCustodian) {
      dispatchFeedback({ type: 'TOGGLE_IS_DRAGGING', payload: true });
      event.currentTarget.style.border = 'var(--drag-and-drop-div-wide-border)';
      event.currentTarget.style.opacity = 'var(--drag-and-drop-div-low-opacity)';
    }
  };

  const onImportFileError = async ({ xhr }) => {
    if (xhr.status === 423) {
      notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
    }
  };

  const onKeyChange = event => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      onSendMessage(event.target.value);
    }
    if (event.key === 'Enter' && event.shiftKey) {
      event.preventDefault();
      dispatchFeedback({ type: 'ON_UPDATE_MESSAGE', payload: { value: `${messageToSend} \r\n` } });
    }
  };

  const onLoadMessages = async (dataProviderId, page) => {
    try {
      const { data } = await FeedbackService.getAllMessages(dataflowId, page, dataProviderId);
      return { messages: data, unreadMessages: data.filter(msg => !msg.read) };
    } catch (error) {
      console.error('Feedback - onLoadMessages.', error);
    }
  };

  const onLoadDataProviders = async () => {
    const responseRepresentatives = await RepresentativeService.getRepresentatives(dataflowId);
    const responseDataProviders = await RepresentativeService.getDataProviders(responseRepresentatives.group);

    const filteredDataProviders = responseDataProviders.filter(dataProvider =>
      responseRepresentatives.representatives.some(
        representative => representative.dataProviderId === dataProvider.dataProviderId
      )
    );

    dispatchFeedback({ type: 'SET_DATAPROVIDERS', payload: filteredDataProviders });
  };

  const onSendMessage = async message => {
    if (message.trim() !== '') {
      try {
        dispatchFeedback({ type: 'SET_IS_SENDING', payload: true });
        const messageCreated = await FeedbackService.createMessage(
          dataflowId,
          message,
          isCustodian && !isEmpty(selectedDataProvider)
            ? selectedDataProvider.dataProviderId
            : parseInt(representativeId)
        );
        if (messageCreated.data) {
          dispatchFeedback({ type: 'ON_SEND_MESSAGE', payload: { value: { ...messageCreated.data } } });
        }
      } catch (error) {
        console.error('Feedback - onSendMessage.', error);
      } finally {
        dispatchFeedback({ type: 'SET_IS_SENDING', payload: false });
      }
    }
  };

  const onUpdateNewMessageAdded = payload => {
    dispatchFeedback({ type: 'ON_UPDATE_NEW_MESSAGE_ADDED', payload });
  };

  const onUpload = async () => {
    dispatchFeedback({ type: 'TOGGLE_FILE_UPLOAD_VISIBILITY', payload: false });
    // const {
    //   dataflow: { name: dataflowName },
    //   dataset: { name: datasetName }
    // } = await MetadataUtils.getMetadata({ dataflowId, datasetId });
    // notificationContext.add({
    //   type: 'DATASET_DATA_LOADING_INIT',
    //   content: {
    //     datasetLoadingMessage: resources.messages['datasetLoadingMessage'],
    //     title: TextUtils.ellipsis(tableName, config.notifications.STRING_LENGTH_MAX),
    //     datasetLoading: resources.messages['datasetLoading'],
    //     dataflowName,
    //     datasetName
    //   }
    // });
    dispatchFeedback({ type: 'RESET_DRAGGED_FILES' });
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  return layout(
    <Fragment>
      <Title
        icon="comments"
        iconSize="3.5rem"
        subtitle={dataflowName}
        title={`${resources.messages['technicalFeedback']} `}
      />
      <div className={`${styles.feedbackWrapper} feedback-wrapper-help-step`}>
        {isCustodian && (
          <div className={`${styles.dataProviderWrapper} feedback-dataProvider-help-step`}>
            <ListBox
              ariaLabel="dataProviders"
              className={styles.dataProvider}
              onChange={e => {
                onChangeDataProvider(e.target.value);
              }}
              optionLabel="label"
              options={dataProviders}
              title={resources.messages['feedbackDataProvider']}
              value={selectedDataProvider}></ListBox>
          </div>
        )}
        {isCustodian && !isEmpty(selectedDataProvider) && isDragging && (
          <span className={styles.dragAndDropFileMessage}>{resources.messages['dragAndDropFileMessage']}</span>
        )}
        <div
          className={`${styles.listMessagesWrapper} ${
            isCustodian ? styles.flexBasisCustodian : styles.flexBasisProvider
          }`}
          onDragLeave={isCustodian && !isEmpty(selectedDataProvider) && onDragLeave}
          onDragOver={isCustodian && !isEmpty(selectedDataProvider) && onDragOver}
          onDrop={isCustodian && !isEmpty(selectedDataProvider) && onDrop}
          // onDragStart={onDragStart}
        >
          <ListMessages
            canLoad={(isCustodian && !isEmpty(selectedDataProvider)) || !isCustodian}
            className={`feedback-messages-help-step`}
            emptyMessage={
              isCustodian && isEmpty(selectedDataProvider)
                ? resources.messages['noMessagesCustodian']
                : resources.messages['noMessages']
            }
            isCustodian={isCustodian}
            isLoading={isLoading}
            messageFirstLoad={feedbackState.messageFirstLoad}
            messages={messages}
            newMessageAdded={newMessageAdded}
            onFirstLoadMessages={onFirstLoadMessages}
            onLazyLoad={onGetMoreMessages}
            onUpdateNewMessageAdded={onUpdateNewMessageAdded}
          />
          {!isCustodian && (
            <label className={styles.helpdeskMessage}>{resources.messages['feedbackHelpdeskMessage']}</label>
          )}
          {isCustodian && (
            <div className={`${styles.sendMessageWrapper} feedback-send-message-help-step`}>
              <InputTextarea
                className={styles.sendMessageTextarea}
                collapsedHeight={50}
                disabled={isCustodian && isEmpty(selectedDataProvider)}
                id="feedbackSender"
                key="feedbackSender"
                onChange={e => dispatchFeedback({ type: 'ON_UPDATE_MESSAGE', payload: { value: e.target.value } })}
                onKeyDown={e => onKeyChange(e)}
                placeholder={
                  isCustodian && isEmpty(selectedDataProvider)
                    ? resources.messages['noMessagesCustodian']
                    : resources.messages['writeMessagePlaceholder']
                }
                value={messageToSend}
              />
              <Button
                className={`${
                  (isCustodian && isEmpty(selectedDataProvider)) || isSending ? '' : 'p-button-animated-right-blink'
                } p-button-secondary ${styles.attachFileMessageButton}`}
                disabled={(isCustodian && isEmpty(selectedDataProvider)) || isSending}
                icon="clipboard"
                iconPos="right"
                label={resources.messages['uploadAttachment']}
                onClick={() => dispatchFeedback({ type: 'TOGGLE_FILE_UPLOAD_VISIBILITY', payload: true })}
              />
              <Button
                className={`${
                  messageToSend === '' || (isCustodian && isEmpty(selectedDataProvider)) || isSending
                    ? ''
                    : 'p-button-animated-right-blink'
                } p-button-primary ${styles.sendMessageButton}`}
                disabled={messageToSend === '' || (isCustodian && isEmpty(selectedDataProvider)) || isSending}
                icon="comment"
                iconPos="right"
                label={resources.messages['send']}
                onClick={() => onSendMessage(messageToSend)}
              />
            </div>
          )}
        </div>
      </div>
      {importFileDialogVisible && (
        <CustomFileUpload
          accept="*"
          chooseLabel={resources.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
          dialogHeader={`${resources.messages['uploadAttachment']} to ${selectedDataProvider.label}`}
          dialogOnHide={() => dispatchFeedback({ type: 'TOGGLE_FILE_UPLOAD_VISIBILITY', payload: false })}
          dialogVisible={importFileDialogVisible}
          draggedFiles={draggedFiles}
          infoTooltip={`${resources.messages['supportedFileExtensionsTooltip']} any`}
          invalidExtensionMessage={resources.messages['invalidExtensionFile']}
          isDialog={true}
          name="file"
          onError={onImportFileError}
          onUpload={onUpload}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(FeedbackConfig.importFile, {
            dataflowId,
            providerId:
              isCustodian && !isEmpty(selectedDataProvider)
                ? selectedDataProvider.dataProviderId
                : parseInt(representativeId)
          })}`}
        />
      )}
    </Fragment>
  );
});
