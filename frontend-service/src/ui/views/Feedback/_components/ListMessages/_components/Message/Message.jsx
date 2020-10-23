import React from 'react';

import styles from './Message.module.scss';

export const Message = ({ message }) => {
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

  // <div key={message.id}>{message.message}</div>;
};
