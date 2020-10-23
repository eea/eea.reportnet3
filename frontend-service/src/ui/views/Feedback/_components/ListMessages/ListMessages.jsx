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
    dispatchListMessages({ type: 'SET_IS_LOADING', payload: false });
  }, [messages])

  const onScroll = e => {    
    if (!isNil(e)) {
      if (e.target.scrollTop <= 0 && lazyLoading) {
        dispatchListMessages({ type: 'SET_IS_LOADING', payload: true });
        onLazyLoad(10, 20);
        // messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight/2;
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
      {console.log(messages)}
      {messages.map(message => (
        <Message message={message} />
      ))}
    </div>
  );
};
