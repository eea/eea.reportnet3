import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { FeedbackRequesterEmptyHelpConfig } from 'conf/help/feedback/requester/empty';

import styles from './Feedback.module.scss';

import { Button } from 'ui/views/_components/Button';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';
import { ListMessages } from './_components/ListMessages';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
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
    messages: [],
    messageToSend: '',
    newMessageAdded: false,
    selectedDataProvider: {}
  });

  const {
    currentPage,
    dataflowName,
    dataProviders,
    isCustodian,
    isLoading,
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
    if (isCustodian) {
      if (isEmpty(messages)) {
      } else {
      }
    } else {
    }
    leftSideBarContext.addHelpSteps(
      isCustodian ? FeedbackRequesterEmptyHelpConfig : FeedbackRequesterEmptyHelpConfig,
      'dataflowHelpHelp'
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
      const isCustodian = userContext.hasPermission([config.permissions.DATA_CUSTODIAN]);
      dispatchFeedback({ type: 'SET_IS_CUSTODIAN', payload: isCustodian });
    }
  }, [userContext]);

  useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_FEEDBACK, dataflowId, history });

  const onChangeDataProvider = value => {
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
      const marked = await FeedbackService.markAsRead(
        dataflowId,
        data.unreadMessages
          .filter(unreadMessage => (isCustodian ? unreadMessage.direction : !unreadMessage.direction))
          .map(unreadMessage => {
            return { id: unreadMessage.id, read: true };
          })
      );
    }

    dispatchFeedback({ type: 'SET_MESSAGES', payload: data.messages });
  };

  const onKeyChange = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onSendMessage(event.target.value);
    }
  };

  const onLoadMessages = async (dataProviderId, page) => {
    const data = await FeedbackService.loadMessages(dataflowId, page, dataProviderId);
    return { messages: data, unreadMessages: data.filter(msg => !msg.read) };
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
        const messageCreated = await FeedbackService.create(
          dataflowId,
          message,
          isCustodian && !isEmpty(selectedDataProvider)
            ? selectedDataProvider.dataProviderId
            : parseInt(representativeId)
        );
        if (messageCreated) {
          dispatchFeedback({
            type: 'ON_SEND_MESSAGE',
            payload: {
              value: { ...messageCreated }
            }
          });
        }
      } catch (error) {
        console.error(error);
      }
    }
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
        title={`${resources.messages['dataflowFeedback']} `}
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
          className={`${styles.messagesWrapper} ${isCustodian ? styles.flexBasisCustodian : styles.flexBasisProvider}`}>
          {isLoading ? (
            <Spinner className={styles.spinnerLoadingMessages} />
          ) : (
            <ListMessages
              canLoad={(isCustodian && !isEmpty(selectedDataProvider)) || !isCustodian}
              className={`feedback-messages-help-step`}
              emptyMessage={`${resources.messages['noMessages']} ${
                isCustodian && isEmpty(selectedDataProvider) ? resources.messages['noMessagesCustodian'] : ''
              }`}
              messages={messages}
              newMessageAdded={newMessageAdded}
              onLazyLoad={onGetMoreMessages}
            />
          )}
          <div className={`${styles.sendMessageWrapper} feedback-send-message-help-step`}>
            <InputTextarea
              // autoFocus={true}
              className={styles.sendMessageTextarea}
              collapsedHeight={100}
              // expandableOnClick={true}
              id="feedbackSender"
              key="feedbackSender"
              onChange={e => dispatchFeedback({ type: 'ON_UPDATE_MESSAGE', payload: { value: e.target.value } })}
              onKeyDown={e => onKeyChange(e)}
              placeholder={resources.messages['writeMessagePlaceholder']}
              value={messageToSend}
            />
            <Button
              className={`p-button-animated-right-blink p-button-primary ${styles.sendMessageButton}`}
              disabled={messageToSend === ''}
              label={resources.messages['send']}
              icon={'comment'}
              iconPos="right"
              onClick={() => onSendMessage(messageToSend)}
            />
          </div>
        </div>
      </div>
    </Fragment>
  );
});
