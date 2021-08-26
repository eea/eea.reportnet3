import { Fragment, useContext } from 'react';
import dayjs from 'dayjs';

import styles from './Message.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { FeedbackService } from 'services/FeedbackService';

import { FileUtils } from 'views/_functions/Utils/FileUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Message = ({
  attachment = {
    extension: '',
    name: '',
    size: ''
  },
  dataflowId,
  hasSeparator,
  isAttachment = false,
  isCustodian,
  message,
  onToggleVisibleDeleteMessage
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

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

  const onFileDownload = async (dataflowId, messageId, dataProviderId) => {
    try {
      const data = await FeedbackService.getMessageAttachment(dataflowId, messageId, dataProviderId);
      DownloadFile(data, attachment.name);
    } catch (error) {
      console.error('Message - onFileDownload.', error);
      notificationContext.add({
        type: 'FEEDBACK_DOWNLOAD_MESSAGE_ATTACHMENT_ERROR',
        content: {}
      });
    }
  };

  const renderAttachment = () => {
    const { bytesParsed, sizeType } = FileUtils.formatBytes(attachment.size);
    return (
      <div className={styles.messageAttachment}>
        <div className={styles.messageAttachmentFile}>
          <div>
            <FontAwesomeIcon icon={AwesomeIcons(attachment.extension)} role="presentation" />
            <span data-for="fileName" data-tip>
              {attachment.name.length > 45 ? `${attachment.name.substring(0, 45)}...` : attachment.name}
            </span>
          </div>
          <Button
            className={`p-button-animated-right-blink p-button-secondary-transparent ${styles.downloadFileButton}`}
            icon="export"
            iconPos="right"
            onClick={() => onFileDownload(dataflowId, message.id, message.providerId)}
            style={{ color: message.direction ? 'var(--white)' : 'var(--c-black-400)' }}
            tooltip={`${resourcesContext.messages['downloadFile']}: ${attachment.name}`}
            tooltipOptions={{ position: 'top' }}
          />
        </div>
        <div className={styles.messageAttachmentFileData}>
          <span>{`.${attachment.extension.toUpperCase()} `}</span>
          <span>{`${bytesParsed} ${sizeType}`}</span>
        </div>
      </div>
    );
  };

  const renderMessage = () => {
    return (
      <div className={`${styles.message} rep-feedback-message ${getStyles()}`} key={message.id}>
        <div className={styles.messageTextWrapper}>
          {isAttachment ? (
            renderAttachment()
          ) : (
            <span
              className={`${styles.messageText} ${message.direction ? styles.sender : styles.receiver}`}
              dangerouslySetInnerHTML={{ __html: message.content.replace(/(?:\r\n|\r|\n)/g, '<br/>') }}></span>
          )}
          <span className={styles.datetime}>{dayjs(message.date).format('YYYY-MM-DD HH:mm')}</span>
        </div>
        {isCustodian && (
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
    <div className={styles.unreadSeparator}>{`${resourcesContext.messages['unreadMessageSeparator']} (${dayjs(
      message.date
    ).format('YYYY-MM-DD HH:mm')})`}</div>
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
