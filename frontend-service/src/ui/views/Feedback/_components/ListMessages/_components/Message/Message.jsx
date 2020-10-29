import React, { useContext } from 'react';
import dayjs from 'dayjs';

import styles from './Message.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Message = ({ hasSeparator, message }) => {
  const resources = useContext(ResourcesContext);

  const renderMessage = () => {
    return (
      <div className={styles.messageWrapper} key={message.id}>
        <div className={`${styles.message} ${message.direction ? styles.sender : styles.receiver}`}>
          <div className={styles.messageTextWrapper}>
            <span className={styles.datetime}>{dayjs(message.date).format('YYYY-MM-DD HH:mm:ss')}</span>
            <span className={`${styles.messageText} ${message.direction ? styles.sender : styles.receiver}`}>
              {message.content}
            </span>
          </div>
        </div>
      </div>
    );
  };

  const renderSeparator = () => {
    return (
      <div
        className={styles.unreadSeparator}>{`${resources.messages['unreadMessageSeparator']} (${message.date})`}</div>
    );
  };

  return hasSeparator ? (
    <>
      {renderSeparator()} {renderMessage()}
    </>
  ) : (
    renderMessage()
  );
};
