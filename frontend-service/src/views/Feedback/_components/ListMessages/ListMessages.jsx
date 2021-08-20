import { useEffect, useReducer, useRef } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';
import uniqueId from 'lodash/uniqueId';

import styles from './ListMessages.module.scss';

import { Message } from './_components/Message';
import { Spinner } from 'views/_components/Spinner';

import { listMessagesReducer } from './_functions/Reducers/listMessagesReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ListMessages = ({
  canLoad = true,
  className = '',
  emptyMessage = '',
  isCustodian,
  isLoading,
  lazyLoading = true,
  messageFirstLoad,
  messages = [],
  newMessageAdded,
  onFirstLoadMessages,
  onLazyLoad,
  onUpdateNewMessageAdded
}) => {
  const messagesWrapperRef = useRef();

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isLoadingNewMessages: false,
    separatorIndex: -1,
    listContent: null,
    resetScrollStates: null
  });

  const { isLoadingNewMessages, separatorIndex } = listMessagesState;

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
        {console.log(messages)}
        {messages.map((message, i) => (
          <Message
            attachment={message.messageAttachment}
            hasSeparator={
              i === separatorIndex && ((isCustodian && message.direction) || (!isCustodian && !message.direction))
            }
            isAttachment={TextUtils.areEquals(message.type, 'ATTACHMENT')}
            key={uniqueId('message_')}
            message={message}
          />
        ))}
      </div>
    );
  };

  return (
    <div className={`${styles.messagesWrapper} ${className}`} onScroll={onScroll} ref={messagesWrapperRef}>
      {listMessagesState.listContent}
    </div>
  );
};
