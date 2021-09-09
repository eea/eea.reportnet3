import { Fragment, useContext } from 'react';
import dayjs from 'dayjs';

import styles from './Message.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { FeedbackService } from 'services/FeedbackService';

import { FileUtils } from 'views/_functions/Utils/FileUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Message = ({ dataflowId, hasSeparator, isCustodian, message, onToggleVisibleDeleteMessage }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const getStyles = () => {
    const accStyles = [];
    if (message.automatic) {
      accStyles.push(styles.automatic);
    }

    if (isCustodian) {
      if (!message.direction) {
        accStyles.push(styles.sender);
      } else {
        accStyles.push(styles.receiver);
      }
    } else {
      if (!message.direction) {
        accStyles.push(styles.receiver);
      } else {
        accStyles.push(styles.sender);
      }
    }
    return accStyles.join(' ');
  };

  const onFileDownload = async (dataflowId, messageId, dataProviderId) => {
    try {
      const data = await FeedbackService.getMessageAttachment(dataflowId, messageId, dataProviderId);
      DownloadFile(data, message.messageAttachment.name);
    } catch (error) {
      console.error('Message - onFileDownload.', error);
      notificationContext.add({
        type: 'FEEDBACK_DOWNLOAD_MESSAGE_ATTACHMENT_ERROR',
        content: {}
      });
    }
  };

  const renderAttachment = () => {
    const { bytesParsed, sizeType } = FileUtils.formatBytes(message.messageAttachment.size);
    return (
      <div className={styles.messageAttachment}>
        <div className={styles.messageAttachmentFile}>
          <div>
            <FontAwesomeIcon icon={AwesomeIcons(message.messageAttachment.extension)} role="presentation" />
            <span data-for="fileName" data-tip>
              {message.messageAttachment.name.length > 45
                ? `${message.messageAttachment.name.substring(0, 45)}...`
                : message.messageAttachment.name}
            </span>
          </div>
          {!message.automatic && (
            <Button
              className={`p-button-animated-right-blink p-button-secondary-transparent ${styles.downloadFileButton}`}
              icon="export"
              iconPos="right"
              onClick={() => onFileDownload(dataflowId, message.id, message.providerId)}
              style={{ color: message.direction ? 'var(--white)' : 'var(--c-black-400)' }}
              tooltip={`${resourcesContext.messages['downloadFile']}: ${message.messageAttachment.name}`}
              tooltipOptions={{ position: 'top' }}
            />
          )}
        </div>
        <div className={styles.messageAttachmentFileData}>
          <span>{`.${message.messageAttachment.extension.toUpperCase()} `}</span>
          <span>{`${bytesParsed} ${sizeType}`}</span>
        </div>
      </div>
    );
  };

  const renderMessage = () => {
    return (
      <div className={`${styles.message} rep-feedback-message ${getStyles()}`} key={message.id}>
        <div className={styles.messageTextWrapper}>
          {TextUtils.areEquals(message.type, 'ATTACHMENT') ? (
            renderAttachment()
          ) : (
            <span
              className={`${styles.messageText} ${message.direction ? styles.sender : styles.receiver}`}
              dangerouslySetInnerHTML={{ __html: message.content.replace(/(?:\r\n|\r|\n)/g, '<br/>') }}></span>
          )}
          <span className={styles.datetime}>{dayjs(message.date).format('YYYY-MM-DD HH:mm')}</span>
        </div>
        {isCustodian && !message.automatic && (
          <FontAwesomeIcon
            className={styles.deleteMessageButton}
            icon={AwesomeIcons('deleteCircle')}
            onClick={() => onToggleVisibleDeleteMessage(true, message.id)}
            role="presentation"
          />
        )}
      </div>
    );
  };

  const renderSeparator = () => (
    <div className={`rep-feedback-unreadSeparator ${styles.unreadSeparator}`}>{`${
      resourcesContext.messages['unreadMessageSeparator']
    } (${dayjs(message.date).format('YYYY-MM-DD HH:mm')})`}</div>
  );

  return hasSeparator ? (
    <Fragment>
      {renderSeparator()}
      {renderMessage()}
    </Fragment>
  ) : (
    renderMessage()
  );
};
