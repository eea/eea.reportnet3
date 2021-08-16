import { Fragment, useContext } from 'react';
import dayjs from 'dayjs';

import { config } from 'conf';

import styles from './Message.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

import { FeedbackService } from 'services/FeedbackService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

export const Message = ({
  attachment = {
    extension: 'csv',
    name: 'A very very long file name to see how is displayed test.csv',
    size: '23Mb'
  },
  hasSeparator,
  isAttachment = false,
  message
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  const isCustodian = userContext.hasPermission([
    config.permissions.roles.CUSTODIAN.key,
    config.permissions.roles.STEWARD.key
  ]);

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

  const onFileDownload = async (dataflowId, messageAttachmentId, dataProviderId) => {
    try {
      const { data } = await FeedbackService.getMessageAttachment(dataflowId, messageAttachmentId, dataProviderId);
      DownloadFile(data, attachment.name);
    } catch (error) {
      console.error('Message - onFileDownload.', error);
    }
  };

  const renderAttachment = () => {
    return (
      <div className={styles.messageAttachment}>
        <div className={styles.messageAttachmentFile}>
          <div>
            <FontAwesomeIcon icon={AwesomeIcons(attachment.extension)} role="presentation" />
            <span data-for="fileName" data-tip>
              {attachment.name.length > 45 ? `${attachment.name.substring(0, 45)}...` : attachment.name}
            </span>
            <ReactTooltip effect="solid" id="fileName" place="top">
              {attachment.name}
            </ReactTooltip>
          </div>
          <Button
            className={`p-button-animated-right-blink p-button-secondary-transparent ${styles.downloadFileButton}`}
            icon="export"
            iconPos="right"
            onClick={() => onFileDownload(attachment.name, message.fieldId)}
            style={{ color: message.direction ? 'var(--white)' : 'var(--c-black-400)' }}
            tooltip={resources.messages['downloadFile']}
            tooltipOptions={{ position: 'top' }}
          />
        </div>
        <div className={styles.messageAttachmentFileData}>
          <span>{`.${attachment.extension.toUpperCase()} `}</span>
          <span>{attachment.size}</span>
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
    <Fragment>
      {renderSeparator()} {renderMessage()}
    </Fragment>
  ) : (
    renderMessage()
  );
};
