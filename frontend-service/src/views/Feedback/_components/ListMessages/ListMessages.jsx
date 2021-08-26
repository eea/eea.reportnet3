import { useContext, useEffect, useReducer, useRef } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';
import uniqueId from 'lodash/uniqueId';

import styles from './ListMessages.module.scss';

import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Message } from './_components/Message';
import { Spinner } from 'views/_components/Spinner';

import { listMessagesReducer } from './_functions/Reducers/listMessagesReducer';

import { FeedbackService } from 'services/FeedbackService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ListMessages = ({
  canLoad = true,
  className = '',
  dataflowId,
  emptyMessage = '',
  isCustodian,
  isLoading,
  lazyLoading = true,
  messageFirstLoad,
  messages = [],
  newMessageAdded,
  onFirstLoadMessages,
  onLazyLoad,
  onMessageDelete,
  onUpdateNewMessageAdded,
  providerId
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const messagesWrapperRef = useRef();

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isVisibleConfirmDelete: false,
    isLoadingNewMessages: false,
    listContent: null,
    messageIdToDelete: null,
    resetScrollStates: null,
    separatorIndex: -1
  });

  const { isLoadingNewMessages, isVisibleConfirmDelete, messageIdToDelete, separatorIndex } = listMessagesState;

  useEffect(() => {
    if (!isNil(messagesWrapperRef)) {
      if (messages.length === 1) {
        messagesWrapperRef.current.scrollTop = 1;
      } else {
        messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight;
      }
    }
  }, []);

  useEffect(() => {
    dispatchListMessages({ type: 'SET_SEPARATOR_INDEX', payload: getIndexByHeader(messages) });
  }, [messages]);

  useEffect(() => {
    dispatchListMessages({
      type: 'SET_LIST_CONTENT',
      payload: renderMessageList()
    });
  }, [isLoading, isLoadingNewMessages, messages, separatorIndex]);

  const getIndexByHeader = messagesArray => {
    return messagesArray
      .map(message => {
        return message.read;
      })
      .indexOf(false);
  };

  useEffect(() => {
    if (newMessageAdded || messageFirstLoad) {
      if (messageFirstLoad) {
        dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
      }
      const messages = document.querySelectorAll('.rep-feedback-message');
      if (!isEmpty(messages)) {
        const lastMessage = last(messages);
        messagesWrapperRef.current.scrollTop = lastMessage.offsetTop;
        dispatchListMessages({
          type: 'UPDATE_SCROLL_STATES',
          payload: true
        });
      }
    }
    setTimeout(() => {
      dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
    }, 500);
  }, [messages, listMessagesState.listContent]);

  useEffect(() => {
    if (listMessagesState.resetScrollStates) {
      onFirstLoadMessages(false);
      onUpdateNewMessageAdded(false);
      dispatchListMessages({
        type: 'UPDATE_SCROLL_STATES',
        payload: false
      });
    }
  }, [listMessagesState.resetScrollStates]);

  const onConfirmDeleteMessage = async () => {
    try {
      await FeedbackService.deleteMessage(dataflowId, messageIdToDelete, providerId);
      dispatchListMessages({
        type: 'ON_TOGGLE_VISIBLE_DELETE_MESSAGE',
        payload: { isVisible: false, messageId: null }
      });
      onMessageDelete(messageIdToDelete);
    } catch (error) {
      console.error('ListMessages - onConfirmDeleteMessage.', error);
      notificationContext.add({
        type: 'FEEDBACK_DELETE_MESSAGE_ERROR',
        content: {}
      });
    }
  };

  const onScroll = e => {
    if (!isNil(e)) {
      if (e.target.scrollTop <= 0 && lazyLoading && canLoad) {
        dispatchListMessages({ type: 'SET_IS_LOADING', payload: true });
        onLazyLoad();
        messagesWrapperRef.current.scrollTop = 5;
      }
    }
  };

  const renderMessageList = () => {
    if (isLoading) {
      return <Spinner className={styles.spinnerLoadingMessages} />;
    }
    if (isEmpty(messages)) {
      return (
        <div className={styles.emptyMessageWrapper}>
          <span>{emptyMessage}</span>
        </div>
      );
    }
    return (
      <div className={styles.scrollMessagesWrapper}>
        {isLoadingNewMessages && (
          <div className={styles.lazyLoadingWrapper}>
            <Spinner className={styles.lazyLoadingSpinner} />
          </div>
        )}
        {messages.map((message, i) => (
          <Message
            attachment={message.messageAttachment}
            dataflowId={dataflowId}
            hasSeparator={
              i === separatorIndex && ((isCustodian && message.direction) || (!isCustodian && !message.direction))
            }
            isAttachment={TextUtils.areEquals(message.type, 'ATTACHMENT')}
            key={uniqueId('message_')}
            message={message}
            onToggleVisibleDeleteMessage={(isVisible, messageId) =>
              dispatchListMessages({ type: 'ON_TOGGLE_VISIBLE_DELETE_MESSAGE', payload: { isVisible, messageId } })
            }
          />
        ))}
      </div>
    );
  };

  return (
    <div className={`${styles.messagesWrapper} ${className}`} onScroll={onScroll} ref={messagesWrapperRef}>
      {listMessagesState.listContent}
      {isVisibleConfirmDelete && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resourcesContext.messages['deleteFeedbackMessageHeader']}`}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onConfirmDeleteMessage}
          onHide={() =>
            dispatchListMessages({
              type: 'ON_TOGGLE_VISIBLE_DELETE_MESSAGE',
              payload: { isVisible: false, messageId: null }
            })
          }
          visible={isVisibleConfirmDelete}>
          {resourcesContext.messages['deleteFeedbackMessage']}
        </ConfirmDialog>
      )}
    </div>
  );
};
