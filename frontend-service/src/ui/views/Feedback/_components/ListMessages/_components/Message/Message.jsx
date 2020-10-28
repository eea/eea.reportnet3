import React, { useContext } from 'react';

import styles from './Message.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Message = ({ hasSeparator, message }) => {
  const resources = useContext(ResourcesContext);

  const renderMessage = () => {
    return (
      <div className={styles.messageWrapper} key={message.id}>
        <div className={`${styles.message} ${message.sender ? styles.sender : styles.receiver}`}>
          <div className={styles.messageTextWrapper}>
            <span className={styles.datetime}>{message.datetime}</span>
            <span className={`${styles.messageText} ${message.sender ? styles.sender : styles.receiver}`}>
              {message.message}
            </span>
          </div>
        </div>
      </div>
    );
  };

  const renderSeparator = () => {
    return (
      <div
        className={
          styles.unreadSeparator
        }>{`${resources.messages['unreadMessageSeparator']} (${message.datetime})`}</div>
    );
  };

  return hasSeparator ? (
    <>
      {renderSeparator()} {renderMessage()}
    </>
  ) : (
    renderMessage()
  );

  // <div key={message.id}>{message.message}</div>;
};
