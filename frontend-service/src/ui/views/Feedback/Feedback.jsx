import React, { Fragment, useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './Feedback.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
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
    params: { dataflowId }
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
      onLoadDataProviders();
    }
  }, [isCustodian]);

  useEffect(() => {
    console.log(selectedDataProvider);
    if (!isNil(isCustodian)) {
      if (isCustodian) {
        if (!isEmpty(selectedDataProvider)) {
          onGetInitialMessages(selectedDataProvider.dataProviderId);
        }
      } else {
        console.log('LLEGO');
        onGetInitialMessages();
      }
    }
  }, [selectedDataProvider]);

  useEffect(() => {
    if (!isNil(userContext.contextRoles)) {
      const userRoles = userContext.getUserRole(`${config.permissions.DATAFLOW}${dataflowId}`);
      dispatchFeedback({
        type: 'SET_IS_CUSTODIAN',
        payload:
          userRoles.includes(config.permissions['DATA_CUSTODIAN']) ||
          userRoles.includes(config.permissions['DATA_STEWARD'])
      });
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
    const data = await onLoadMessages(isCustodian ? selectedDataProvider.dataProviderId : 1, currentPage);
    dispatchFeedback({ type: 'ON_LOAD_MORE_MESSAGES', payload: data });
  };

  const onGetInitialMessages = async dataProviderId => {
    dispatchFeedback({ type: 'SET_IS_LOADING', payload: true });
    const data = await onLoadMessages(dataProviderId, 0);
    dispatchFeedback({ type: 'SET_MESSAGES', payload: data });
  };

  const onKeyChange = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onSendMessage(event.target.value);
    }
  };

  const onLoadMessages = async (dataProviderId, page) => {
    const data = await FeedbackService.loadMessages(dataflowId, page);
    return data;
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

    console.log(allRepresentatives, responseAllDataProviders, filteredDataProviders);
  };

  const onSendMessage = async message => {
    console.log({ message });
    if (message.trim() !== '') {
      //Send message to BE
      try {
        if (isCustodian && !isEmpty(selectedDataProvider)) {
          const created = await FeedbackService.create(dataflowId, message, selectedDataProvider.dataProviderId);
          console.log({ created });
          if (created) {
            dispatchFeedback({
              type: 'ON_SEND_MESSAGE',
              payload: {
                value: {
                  datetime: dayjs(Date.now()).format('YYYY-MM-DD HH:mm:ss'),
                  id: messages.length + 1,
                  message,
                  read: false,
                  sender: true
                }
              }
            });
          }
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
        icon="info"
        iconSize="3.5rem"
      />
      {isCustodian && (
        <div className={styles.dataProviderWrapper}>
          <span>{resources.messages['manageRolesDialogDataProviderColumn']}</span>
          <Dropdown
            className={styles.dataProvider}
            onChange={e => {
              onChangeDataProvider(e.target.value);
            }}
            optionLabel="label"
            options={dataProviders}
            value={selectedDataProvider}
          />
        </div>
      )}
      <div className={styles.feedbackWrapper}>
        {isLoading ? (
          <Spinner className={styles.spinnerLoadingMessages} />
        ) : (
          <ListMessages
            emptyMessage={`${resources.messages['noMessages']} ${
              isCustodian && isEmpty(selectedDataProvider) ? resources.messages['noMessagesCustodian'] : ''
            }`}
            messages={messages}
            newMessageAdded={newMessageAdded}
            onLazyLoad={onGetMoreMessages}
          />
        )}
        <InputTextarea
          // autoFocus={true}
          className={`${styles.sendMessageTextarea} feedback-send-message-help-step`}
          collapsedHeight={55}
          expandableOnClick={true}
          id="feedbackSender"
          key="feedbackSender"
          onChange={e => dispatchFeedback({ type: 'ON_UPDATE_MESSAGE', payload: { value: e.target.value } })}
          onKeyDown={e => onKeyChange(e)}
          placeholder={resources.messages['writeMessagePlaceholder']}
          value={messageToSend}
        />
        <Button
          className={`p-button-animated-right-blink p-button-primary ${styles.sendMessageButton}`}
          label={resources.messages['send']}
          icon={'comment'}
          iconPos="right"
          onClick={() => onSendMessage(messageToSend)}
        />
      </div>
    </Fragment>
  );
});
