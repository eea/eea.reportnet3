import { useContext } from 'react';
import dayjs from 'dayjs';

import { config } from 'conf';

import styles from './Message.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const Message = ({
  attachment = { fileName: 'test.csv', fileSize: '23Mb' },
  hasSeparator,
  isAttachment = true,
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

  const onFileDownload = async (fileName, fieldId) => {
    try {
      // const { data } = await DatasetService.downloadFileData(dataflowId, datasetId, fieldId, dataProviderId);
      // DownloadFile(data, fileName);
    } catch (error) {
      console.error('error', error);
    }
  };

  const renderAttachment = () => {
    return (
      <div className={styles.messageAttachment}>
        <div className={styles.messageAttachmentFile}>
          <div>
            <FontAwesomeIcon icon={AwesomeIcons('file')} role="presentation" />
            <span>{attachment.fileName}</span>
          </div>
          <Button
            className={`p-button-animated-right-blink p-button-secondary-transparent ${styles.downloadFileButton}`}
            icon="export"
            iconPos="right"
            onClick={() => onFileDownload(attachment.fileName, attachment.fieldId)}
            style={{ color: message.direction ? 'var(--white)' : 'var(--c-black-400)' }}
            tooltip={resources.messages['downloadFile']}
            tooltipOptions={{ position: 'top' }}
          />
        </div>

        <div className={styles.messageAttachmentFileData}>
          <span>{`.${attachment.fileName.split('.')[1].toUpperCase()} `}</span>
          <span>{attachment.fileSize}</span>
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
    <>
      {renderSeparator()} {renderMessage()}
    </>
  ) : (
    renderMessage()
  );
};
