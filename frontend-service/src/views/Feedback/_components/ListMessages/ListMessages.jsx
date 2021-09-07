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

export const ListMessages = ({
  canLoad = true,
  className = '',
  dataflowId,
  emptyMessage = '',
  isCustodian,
  isLoading,
  lazyLoading = true,
  messages = [],
  moreMessagesLoaded,
  moreMessagesLoading,
  newMessageAdded,
  onLazyLoad,
  onMessageDelete,
  onUpdateNewMessageAdded,
  providerId
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const listMessagesWrapperRef = useRef();
  const messagesWrapperRef = useRef();

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isVisibleConfirmDelete: false,
    isLoadingNewMessages: false,
    listContent: null,
    messageDeleted: false,
    messageIdToDelete: null,
    resetScrollStates: null,
    separatorIndex: -1
  });

  const {
    isLoadingNewMessages,
    isVisibleConfirmDelete,
    messageDeleted,
    messageIdToDelete,
    separatorIndex
  } = listMessagesState;

  useEffect(() => {
    dispatchListMessages({ type: 'SET_SEPARATOR_INDEX', payload: getIndexByHeader(messages) });
  }, [messages]);

  useEffect(() => {
    dispatchListMessages({
      type: 'SET_LIST_CONTENT',
      payload: renderMessageList()
    });
  }, [isCustodian, isLoading, isLoadingNewMessages, messages, separatorIndex]);

  const getIndexByHeader = messagesArray => {
    return messagesArray
      .map(message => {
        return message.read;
      })
      .indexOf(false);
  };

  useEffect(() => {
    const domMessages = document.querySelectorAll('.rep-feedback-message');
    const lastMessage = last(domMessages);

    if (!isCustodian) {
      if (!moreMessagesLoaded) {
        const unreadSeparator = document.querySelectorAll('.rep-feedback-unreadSeparator');
        if (!isEmpty(unreadSeparator)) {
          const lastSeparator = last(unreadSeparator);
          messagesWrapperRef.current.scrollTo(0, lastSeparator.offsetTop - 100);
        } else {
          if (!moreMessagesLoading) {
            messagesWrapperRef.current.scrollTo(0, lastMessage?.offsetTop);
          }
        }
      } else {
        messagesWrapperRef.current.scrollTo(0, 5);
      }
    } else {
      console.log(messageDeleted);
      if (newMessageAdded || messageDeleted) {
        messagesWrapperRef.current.scrollTo(0, lastMessage.offsetTop);
        if (messageDeleted) {
          dispatchListMessages({ type: 'SET_IS_MESSAGE_DELETED', payload: false });
        }
      } else {
        if (!moreMessagesLoaded) {
          if (!isEmpty(domMessages)) {
            if (!moreMessagesLoading) {
              messagesWrapperRef.current.scrollTo(0, lastMessage.offsetTop);
            }
          }
        } else {
          messagesWrapperRef.current.scrollTo(0, 5);
        }
      }
    }
    dispatchListMessages({
      type: 'UPDATE_SCROLL_STATES',
      payload: true
    });

    dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
  }, [messages, listMessagesState.listContent]);

  useEffect(() => {
    if (listMessagesState.resetScrollStates) {
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
      <div className={styles.scrollMessagesWrapper} ref={listMessagesWrapperRef}>
        <p className={styles.messageCounter}>{`${messages.length} / 1000`}</p>
        {isLoadingNewMessages && (
          <div className={styles.lazyLoadingWrapper}>
            <Spinner className={styles.lazyLoadingSpinner} />
          </div>
        )}
        {messages.map((message, i) => (
          <Message
            dataflowId={dataflowId}
            hasSeparator={
              i === separatorIndex && ((isCustodian && message.direction) || (!isCustodian && !message.direction))
            }
            isCustodian={isCustodian}
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
