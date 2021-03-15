import React, { useContext } from 'react';
import dayjs from 'dayjs';

import { config } from 'conf';

import styles from './Message.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const Message = ({ hasSeparator, message }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const isCustodian = userContext.hasPermission([config.permissions.DATA_CUSTODIAN, config.permissions.DATA_STEWARD]);

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
      <div key={message.id} className={`${styles.message} rep-feedback-message ${getStyles()}`}>
        <div className={styles.messageTextWrapper}>
          <span
            className={`${styles.messageText} ${message.direction ? styles.sender : styles.receiver}`}
            dangerouslySetInnerHTML={{ __html: message.content.replace(/(?:\r\n|\r|\n)/g, '<br/>') }}></span>
          <span className={styles.datetime}>{dayjs(message.date).format('YYYY-MM-DD HH:mm')}</span>
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
