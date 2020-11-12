import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ListMessages.module.scss';

import { Message } from './_components/Message';
import { Spinner } from 'ui/views/_components/Spinner';

import { listMessagesReducer } from './_functions/Reducers/listMessagesReducer';

export const ListMessages = ({
  canLoad = true,
  className = '',
  emptyMessage = '',
  isCustodian,
  isLoading,
  lazyLoading = true,
  messages = [],
  newMessageAdded,
  onLazyLoad
}) => {
  const messagesWrapperRef = useRef();

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isLoadingNewMessages: false,
    separatorIndex: -1
  });

  const [listContent, setListContent] = useState();

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
    setListContent(renderMessageList());
  }, [isLoading, isLoadingNewMessages, messages]);

  const getIndexByHeader = messagesArray => {
    return messagesArray
      .map(message => {
        return message.read;
      })
      .indexOf(false);
  };

  useEffect(() => {
    dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
    if (newMessageAdded) {
      messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight;
    }
  }, [messages]);

  const onScroll = e => {
    if (!isNil(e)) {
      if (e.target.scrollTop <= 0 && lazyLoading && canLoad) {
        dispatchListMessages({ type: 'SET_IS_LOADING', payload: true });
        onLazyLoad();
        messagesWrapperRef.current.scrollTop = 1;
      }
    }
  };

  const renderMessageList = () => {
    if (isLoading) {
      return <Spinner className={styles.spinnerLoadingMessages} />;
    }
    if (isLoadingNewMessages) {
      return (
        <div className={styles.lazyLoadingWrapper}>
          <Spinner className={styles.lazyLoadingSpinner} />
        </div>
      );
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
        {messages.map((message, i) => (
          <Message
            message={message}
            hasSeparator={
              i === separatorIndex && ((isCustodian && message.direction) || (!isCustodian && !message.direction))
            }
          />
        ))}
      </div>
    );
  };

  return (
    <div className={`${styles.messagesWrapper} ${className}`} onScroll={onScroll} ref={messagesWrapperRef}>
      {listContent}
    </div>
  );
};
