import React, { useEffect, useReducer, useRef } from 'react';
import isNil from 'lodash/isNil';

import styles from './ListMessages.module.scss';

import { Message } from './_components/Message';
import { Spinner } from 'ui/views/_components/Spinner';

import { listMessagesReducer } from './_functions/Reducers/listMessagesReducer';

export const ListMessages = ({ lazyLoading = true, messages, onLazyLoad }) => {
  const messagesWrapperRef = useRef();

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isLoadingNewMessages: false
  });

  const { isLoadingNewMessages } = listMessagesState;

  useEffect(() => {
    if (!isNil(messagesWrapperRef)) {
      messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight;
      // messagesWrapperRef.current.addEventListener = onScroll();
    }
  }, []);

  useEffect(() => {
    dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
    messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight;
  }, [messages]);

  const onScroll = e => {
    if (!isNil(e)) {
      if (e.target.scrollTop <= 0 && lazyLoading) {
        dispatchListMessages({ type: 'SET_IS_LOADING', payload: true });
        onLazyLoad(10, 20);
        messagesWrapperRef.current.scrollTop = 100;
      }
    }
  };

  return (
    <div className={styles.messagesWrapper} onScroll={onScroll} ref={messagesWrapperRef}>
      {isLoadingNewMessages && (
        <div className={styles.lazyLoadingWrapper}>
          <Spinner className={styles.lazyLoadingSpinner} />
        </div>
      )}
      {messages.map(message => (
        <Message message={message} />
      ))}
    </div>
  );
};
