import { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { FeedbackReporterHelpConfig } from 'conf/help/feedback/reporter';
import { FeedbackRequesterHelpConfig } from 'conf/help/feedback/requester';

import styles from './Feedback.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';
import { ListMessages } from './_components/ListMessages';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from 'ui/views/_components/Title';

import { FeedbackService } from 'core/services/Feedback';
import { RepresentativeService } from 'core/services/Representative';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { feedbackReducer } from './_functions/Reducers/feedbackReducer';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { DataflowUtils } from 'ui/views/_functions/Utils/DataflowUtils';

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
    isCustodian: undefined,
    isLoading: false,
    isSending: false,
    messages: [],
    messageToSend: '',
    newMessageAdded: false,
    selectedDataProvider: {},
    messageFirstLoad: false
  });

  const {
    currentPage,
    dataflowName,
    dataProviders,
    isCustodian,
    isLoading,
    isSending,
    messages,
    messageToSend,
    newMessageAdded,
    selectedDataProvider
  } = feedbackState;

  useEffect(() => {
    onGetDataflowName();
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
        onGetInitialMessages(representativeId);
      }
    }
  }, [selectedDataProvider, isCustodian]);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const isCustodian = userContext.hasPermission([
        config.permissions.roles.CUSTODIAN.key,
        config.permissions.roles.STEWARD.key
      ]);
      dispatchFeedback({ type: 'SET_IS_CUSTODIAN', payload: isCustodian });
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

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_FEEDBACK, dataflowId, history });

  const onFirstLoadMessages = loadState => {
    dispatchFeedback({ type: 'ON_UPDATE_MESSAGE_FIRST_LOAD', payload: loadState });
  };

  const onChangeDataProvider = value => {
    if (isNil(value)) {
      dispatchFeedback({ type: 'RESET_MESSAGES', payload: {} });
    } else {
      onFirstLoadMessages(true);
    }

    dispatchFeedback({ type: 'SET_SELECTED_DATAPROVIDER', payload: value });
  };

  const onGetDataflowName = async () => {
    try {
      const name = await DataflowUtils.getDataflowName(dataflowId);
      dispatchFeedback({ type: 'SET_DATAFLOW_NAME', payload: name });
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {}
      });
    }
  };

  const onGetMoreMessages = async () => {
    if ((isCustodian && isEmpty(selectedDataProvider)) || isLoading) return;
    const data = await onLoadMessages(
      isCustodian ? selectedDataProvider.dataProviderId : representativeId,
      currentPage
    );
    dispatchFeedback({ type: 'ON_LOAD_MORE_MESSAGES', payload: data.messages });
  };

  const onGetInitialMessages = async dataProviderId => {
    dispatchFeedback({ type: 'SET_IS_LOADING', payload: true });
    const data = await onLoadMessages(dataProviderId, 0);
    //mark unread messages as read
    if (data.unreadMessages.length > 0) {
      const unreadMessages = data.unreadMessages
        .filter(unreadMessage => (isCustodian ? unreadMessage.direction : !unreadMessage.direction))
        .map(unreadMessage => ({ id: unreadMessage.id, read: true }));
      if (!isEmpty(unreadMessages)) {
        await FeedbackService.markAsRead(dataflowId, unreadMessages);
      }
    }

    dispatchFeedback({ type: 'SET_MESSAGES', payload: data.messages });
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
      const { data } = await FeedbackService.loadMessages(dataflowId, page, dataProviderId);
      return { messages: data, unreadMessages: data.filter(msg => !msg.read) };
    } catch (error) {
      console.error('error', error);
    }
  };

  const onLoadDataProviders = async () => {
    const allRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);
    const responseAllDataProviders = await RepresentativeService.allDataProviders(allRepresentatives.group);

    const filteredDataProviders = responseAllDataProviders.filter(dataProvider =>
      allRepresentatives.representatives.some(
        representative => representative.dataProviderId === dataProvider.dataProviderId
      )
    );

    dispatchFeedback({ type: 'SET_DATAPROVIDERS', payload: filteredDataProviders });
  };

  const onSendMessage = async message => {
    if (message.trim() !== '') {
      try {
        dispatchFeedback({ type: 'SET_IS_SENDING', payload: true });
        const messageCreated = await FeedbackService.create(
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
        console.error(error);
      } finally {
        dispatchFeedback({ type: 'SET_IS_SENDING', payload: false });
      }
    }
  };

  const onUpdateNewMessageAdded = payload => {
    dispatchFeedback({ type: 'ON_UPDATE_NEW_MESSAGE_ADDED', payload });
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
        title={`${resources.messages['technicalFeedback']} `}
        subtitle={dataflowName}
        icon="comments"
        iconSize="3.5rem"
      />
      <div className={`${styles.feedbackWrapper} feedback-wrapper-help-step`}>
        {isCustodian && (
          <div className={`${styles.dataProviderWrapper} feedback-dataProvider-help-step`}>
            <ListBox
              className={styles.dataProvider}
              options={dataProviders}
              onChange={e => {
                onChangeDataProvider(e.target.value);
              }}
              optionLabel="label"
              title={resources.messages['feedbackDataProvider']}
              value={selectedDataProvider}></ListBox>
          </div>
        )}
        <div
          className={`${styles.listMessagesWrapper} ${
            isCustodian ? styles.flexBasisCustodian : styles.flexBasisProvider
          }`}>
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
                // autoFocus={true}
                className={styles.sendMessageTextarea}
                collapsedHeight={50}
                // expandableOnClick={true}
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
                className={`p-button-animated-right-blink p-button-primary ${styles.sendMessageButton}`}
                disabled={messageToSend === '' || (isCustodian && isEmpty(selectedDataProvider)) || isSending}
                label={resources.messages['send']}
                icon={'comment'}
                iconPos="right"
                onClick={() => onSendMessage(messageToSend)}
              />
            </div>
          )}
        </div>
      </div>
    </Fragment>
  );
});
