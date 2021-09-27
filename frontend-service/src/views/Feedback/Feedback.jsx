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

export const Feedback = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, representativeId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [feedbackState, dispatchFeedback] = useReducer(feedbackReducer, {
    currentPage: 0,
    dataflowStateData: {},
    dataflowName: '',
    dataflowType: '',
    dataProviders: [],
    draggedFiles: null,
    importFileDialogVisible: false,
    isAdmin: false,
    isCustodian: undefined,
    isDragging: false,
    isLoading: true,
    isSending: false,
    messages: [],
    messageToSend: '',
    moreMessagesLoaded: false,
    moreMessagesLoading: false,
    newMessageAdded: false,
    selectedDataProvider: {},
    totalMessages: 0
  });

  const {
    currentPage,
    dataflowName,
    dataflowType,
    dataProviders,
    draggedFiles,
    importFileDialogVisible,
    isAdmin,
    isCustodian,
    isDragging,
    isLoading,
    isSending,
    messages,
    messageToSend,
    moreMessagesLoaded,
    moreMessagesLoading,
    newMessageAdded,
    selectedDataProvider,
    totalMessages
  } = feedbackState;

  useEffect(() => {
    onGetDataflowDetails();
    getDataflowData(dataflowId);
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

  useBreadCrumbs({
    currentPage: CurrentPage.DATAFLOW_FEEDBACK,
    dataflowStateData: feedbackState.dataflowStateData,
    dataflowId,
    dataflowType,
    history,
    isLoading,
    representativeId
  });

  const getDataflowData = async dataflowId => {
    try {
      dispatchFeedback({ type: 'SET_IS_LOADING', payload: true });
      const dataflow = await DataflowService.get(dataflowId);
      dispatchFeedback({ type: 'SET_DATAFLOW_DATA', payload: dataflow });
    } catch (error) {
      console.error('Feedback - getDataflowData.', error);
      notificationContext.add({ type: 'LOAD_DATAFLOW_DATA_ERROR' });
    } finally {
      dispatchFeedback({ type: 'SET_IS_LOADING', payload: false });
    }
  };

  const markMessagesAsRead = async data => {
    //mark unread messages as read
    if (data?.unreadMessages.length > 0) {
      const unreadMessages = data.unreadMessages
        .filter(unreadMessage => (isCustodian ? unreadMessage.direction : !unreadMessage.direction))
        .map(unreadMessage => ({ id: unreadMessage.id, read: true }));

      if (!isEmpty(unreadMessages)) {
        await FeedbackService.markMessagesAsRead(dataflowId, unreadMessages);
      }
    }
  };

  const onChangeDataProvider = value => {
    if (isNil(value)) {
      dispatchFeedback({ type: 'RESET_MESSAGES', payload: [] });
    }

    dispatchFeedback({ type: 'SET_SELECTED_DATAPROVIDER', payload: value });
  };

  const onGetDataflowDetails = async () => {
    try {
      const data = await DataflowService.getDetails(dataflowId);
      const name = data.name;

      dispatchFeedback({
        type: 'SET_DATAFLOW_DETAILS',
        payload: { name, dataflowType: data.type }
      });
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
    try {
      dispatchFeedback({ type: 'ON_TOGGLE_LAZY_LOADING', payload: true });
      const data = await onLoadMessages(
        isCustodian ? selectedDataProvider.dataProviderId : representativeId,
        currentPage,
        true
      );
      await markMessagesAsRead(data);
      dispatchFeedback({ type: 'ON_LOAD_MORE_MESSAGES', payload: data.messages });
    } catch (error) {
      console.error('Feedback - onGetMoreMessages.', error);
      notificationContext.add({
        type: 'LOAD_DATASET_FEEDBACK_MESSAGES_ERROR',
        content: {}
      });
    }
  };

  const onGetInitialMessages = async dataProviderId => {
    const data = await onLoadMessages(dataProviderId, 0, false);
    await markMessagesAsRead(data);
    dispatchFeedback({
      type: 'SET_MESSAGES',
      payload: { msgs: !isNil(data) ? data.messages : [], totalMessages: data.totalMessages }
    });
  };

  const onDrop = event => {
    let files = event.dataTransfer ? event.dataTransfer.files : event.target.files;
    if (!isEmpty(files)) {
      if (files[0].size <= config.MAX_ATTACHMENT_SIZE) {
        dispatchFeedback({ type: 'SET_DRAGGED_FILES', payload: files });
      } else {
        notificationContext.add({ type: 'FEEDBACK_MAX_FILE_SIZE_ERROR' });
      }
    } else {
      notificationContext.add({ type: 'FEEDBACK_INVALID_FILE_ERROR' });
    }
    dispatchFeedback({ type: 'TOGGLE_IS_DRAGGING', payload: false });

    event.currentTarget.style.border = '';
    event.currentTarget.style.opacity = '';
  };

  const onDragLeave = event => {
    dispatchFeedback({ type: 'TOGGLE_IS_DRAGGING', payload: false });
    event.currentTarget.style.border = '';
    event.currentTarget.style.opacity = '';
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

  const onLoadMessages = async (dataProviderId, page, isLazyLoad) => {
    try {
      if (!isLazyLoad) {
        dispatchFeedback({ type: 'SET_IS_LOADING', payload: true });
      }
      const data = await FeedbackService.getAllMessages(dataflowId, page, dataProviderId);
      return {
        messages: data.listMessage,
        unreadMessages: data.listMessage.filter(msg => !msg.read),
        totalMessages: data.totalMessages
      };
    } catch (error) {
      console.error('Feedback - onLoadMessages.', error);
    } finally {
      if (!isLazyLoad) {
        dispatchFeedback({ type: 'SET_IS_LOADING', payload: false });
      }
    }
  };

  const onLoadDataProviders = async () => {
    const responseRepresentatives = await RepresentativeService.getRepresentatives(dataflowId);
    const responseDataProviders = await RepresentativeService.getDataProviders(responseRepresentatives.group);

    const filteredDataProviders = responseDataProviders.filter(dataProvider =>
      responseRepresentatives.representatives.some(
        representative => representative.dataProviderId === dataProvider.dataProviderId && representative.hasDatasets
      )
    );

    dispatchFeedback({ type: 'SET_DATAPROVIDERS', payload: filteredDataProviders });
  };

  const onMessageDelete = messageIdToDelete => {
    dispatchFeedback({
      type: 'ON_DELETE_MESSAGE',
      payload: messageIdToDelete
    });
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

  const onUpload = async event =>
    dispatchFeedback({ type: 'ON_SEND_ATTACHMENT', payload: JSON.parse(event.xhr.response) });

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
        title={`${resourcesContext.messages['technicalFeedback']} `}
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
              title={resourcesContext.messages['feedbackDataProvider']}
              value={selectedDataProvider}></ListBox>
          </div>
        )}
        {isCustodian && !isEmpty(selectedDataProvider) && isDragging && (
          <span className={styles.dragAndDropFileMessage}>{resourcesContext.messages['dragAndDropFileMessage']}</span>
        )}
        <div
          className={`${styles.listMessagesWrapper} ${
            isCustodian ? styles.flexBasisCustodian : styles.flexBasisProvider
          }`}
          onDragLeave={isCustodian && !isEmpty(selectedDataProvider) ? onDragLeave : () => {}}
          onDragOver={isCustodian && !isEmpty(selectedDataProvider) ? onDragOver : () => {}}
          onDrop={isCustodian && !isEmpty(selectedDataProvider) ? onDrop : () => {}}>
          <ListMessages
            canLoad={(isCustodian && !isEmpty(selectedDataProvider)) || !isCustodian}
            className={`feedback-messages-help-step`}
            dataflowId={dataflowId}
            emptyMessage={
              !isNil(isCustodian)
                ? !isCustodian
                  ? resourcesContext.messages['noMessages']
                  : isEmpty(selectedDataProvider)
                  ? resourcesContext.messages['noMessagesCustodian']
                  : resourcesContext.messages['noMessages']
                : ''
            }
            isCustodian={isCustodian}
            isLoading={isLoading}
            messages={messages}
            moreMessagesLoaded={moreMessagesLoaded}
            moreMessagesLoading={moreMessagesLoading}
            newMessageAdded={newMessageAdded}
            onLazyLoad={onGetMoreMessages}
            onMessageDelete={onMessageDelete}
            onUpdateNewMessageAdded={onUpdateNewMessageAdded}
            providerId={selectedDataProvider?.dataProviderId}
            totalMessages={totalMessages}
          />
          {!isNil(isCustodian) && !isCustodian && (
            <label className={styles.helpdeskMessage}>{resourcesContext.messages['feedbackHelpdeskMessage']}</label>
          )}
          {!isNil(isCustodian) && isCustodian && (
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
                    ? resourcesContext.messages['noMessagesCustodian']
                    : resourcesContext.messages['writeMessagePlaceholder']
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
                label={resourcesContext.messages['uploadAttachment']}
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
                label={resourcesContext.messages['send']}
                onClick={() => onSendMessage(messageToSend)}
              />
            </div>
          )}
        </div>
      </div>
      {importFileDialogVisible && (
        <CustomFileUpload
          accept="*"
          chooseLabel={resourcesContext.messages['selectFile']}
          className={styles.FileUpload}
          dialogClassName={styles.Dialog}
          dialogHeader={`${resourcesContext.messages['uploadAttachment']} to ${selectedDataProvider.label}`}
          dialogOnHide={() => dispatchFeedback({ type: 'TOGGLE_FILE_UPLOAD_VISIBILITY', payload: false })}
          dialogVisible={importFileDialogVisible}
          draggedFiles={draggedFiles}
          infoTooltip={`${resourcesContext.messages['supportedFileExtensionsTooltip']} any`}
          invalidExtensionMessage={resourcesContext.messages['invalidExtensionFile']}
          isDialog={true}
          maxFileSize={config.MAX_ATTACHMENT_SIZE}
          name="fileAttachment"
          onError={onImportFileError}
          onUpload={onUpload}
          url={`${window.env.REACT_APP_BACKEND}${getUrl(FeedbackConfig.importFile, {
            dataflowId,
            providerId: selectedDataProvider.dataProviderId
          })}`}
        />
      )}
    </Fragment>
  );
});
