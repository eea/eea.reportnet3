import React, { useContext, useEffect, useReducer, useRef } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ListMessages.module.scss';

import { Message } from './_components/Message';
import { Spinner } from 'ui/views/_components/Spinner';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { listMessagesReducer } from './_functions/Reducers/listMessagesReducer';

export const ListMessages = ({
  canLoad = true,
  emptyMessage = '',
  lazyLoading = true,
  messages = [],
  newMessageAdded,
  onLazyLoad
}) => {
  const messagesWrapperRef = useRef();
  const resources = useContext(ResourcesContext);

  const [listMessagesState, dispatchListMessages] = useReducer(listMessagesReducer, {
    isLoadingNewMessages: false,
    separatorIndex: -1
  });

  const { isLoadingNewMessages, separatorIndex } = listMessagesState;

  useEffect(() => {
    if (!isNil(messagesWrapperRef)) {
      messagesWrapperRef.current.scrollTop = messagesWrapperRef.current.scrollHeight;
      // messagesWrapperRef.current.addEventListener = onScroll();
    }
    dispatchListMessages({ type: 'SET_SEPARATOR_INDEX', payload: getIndexByHeader(messages) });
  }, []);

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

  return (
    <div className={styles.messagesWrapper} onScroll={onScroll} ref={messagesWrapperRef}>
      {isLoadingNewMessages && (
        <div className={styles.lazyLoadingWrapper}>
          <Spinner className={styles.lazyLoadingSpinner} />
        </div>
      )}
      {isEmpty(messages) ? (
        <div className={styles.emptyMessageWrapper}>
          <span>{emptyMessage}</span>
        </div>
      ) : (
        messages.map((message, i) => <Message message={message} hasSeparator={i === separatorIndex} />)
      )}
    </div>
  );
};
