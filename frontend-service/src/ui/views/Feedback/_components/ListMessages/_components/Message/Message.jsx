import React, { useContext } from 'react';
import dayjs from 'dayjs';

import { config } from 'conf';

import styles from './Message.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const Message = ({ hasSeparator, message }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const isCustodian = userContext.hasPermission([config.permissions.DATA_CUSTODIAN]);

  const getStyles = () => {
    if (isCustodian) {
      if (!message.direction) {
        return styles.sender;
      } else {
        return styles.receiver;
      }
    } else {
      if (!message.direction) {
        return styles.receiver;
      } else {
        return styles.sender;
      }
    }
  };

  const renderMessage = () => {
    return (
      <div className={styles.messageWrapper} key={message.id}>
        <div className={`${styles.message} ${getStyles()}`}>
          <div className={styles.messageTextWrapper}>
            <span className={styles.datetime}>{dayjs(message.date).format('YYYY-MM-DD HH:mm')}</span>
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
      <div className={styles.unreadSeparator}>{`${resources.messages['unreadMessageSeparator']} (${dayjs(
        message.date
      ).format('YYYY-MM-DD HH:mm')})`}</div>
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
