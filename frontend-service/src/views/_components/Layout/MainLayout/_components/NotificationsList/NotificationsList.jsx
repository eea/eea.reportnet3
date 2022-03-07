import { Fragment, useContext, useEffect, useState } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './NotificationsList.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { DatasetService } from 'services/DatasetService';
import { DataTable } from 'views/_components/DataTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';

import { NotificationService } from 'services/NotificationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

export const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [columns, setColumns] = useState([]);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({ recordsPerPage: 10, firstPageRecord: 0 });
  const [totalRecords, setTotalRecords] = useState(0);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  useEffect(() => {
    const headers = [
      {
        id: 'message',
        header: resourcesContext.messages['message'],
        template: messageTemplate
      },
      {
        id: 'levelError',
        header: resourcesContext.messages['notificationLevel'],
        template: notificationLevelTemplate,
        style: { width: '6rem' }
      },
      {
        id: 'date',
        header: resourcesContext.messages['date']
      },
      {
        id: 'redirectionUrl',
        header: resourcesContext.messages['action'],
        template: linkTemplate
      }
    ];

    const columnsArray = headers.map(col => (
      <Column body={col.template} field={col.id} header={col.header} key={col.id} style={col.style} />
    ));

    setColumns(columnsArray);
  }, []);

  useEffect(() => {
    if (!isEmpty(columns)) {
      onLoadNotifications(0, paginationInfo.recordsPerPage);
    }
  }, [columns]);

  const getValidUrl = (url = '') => {
    const newUrl = window.decodeURIComponent(url).trim().replace(/\s/g, '');

    if (/^(:\/\/)/.test(newUrl)) {
      return `http${newUrl}`;
    } else if (!/^(f|ht)tps?:\/\//i.test(newUrl)) {
      return `//${newUrl}`;
    } else {
      return newUrl;
    }
  };

  const linkTemplate = rowData => {
    if (rowData.downloadButton) {
      return rowData.downloadButton;
    }

    return (
      rowData.redirectionUrl !== '' && (
        <a href={getValidUrl(rowData.redirectionUrl)} rel="noopener noreferrer" target="_self">
          {rowData.redirectionUrl}
        </a>
      )
    );
  };

  const messageTemplate = rowData => (
    <label
      className={styles.label}
      dangerouslySetInnerHTML={{
        __html: rowData.message
      }}></label>
  );

  const notificationLevelTemplate = rowData =>
    !isNil(rowData.levelError) && (
      <div className={styles.notificationLevelTemplateWrapper}>
        <LevelError type={rowData.levelError.toLowerCase()} />
      </div>
    );

  const onChangePage = event => {
    setPaginationInfo({ ...paginationInfo, recordsPerPage: event.rows, firstPageRecord: event.first });
    onLoadNotifications(event.first, event.rows);
  };

  const onHideNotificationsList = () => {
    setIsNotificationVisible(false);
    notificationContext.deleteAll();
  };

  const notificationsFooter = (
    <div>
      <Button
        className="p-button-rounded p-button-secondary"
        icon="trash"
        label={resourcesContext.messages['deleteUsersNotificationsData']}
        onClick={() => setIsDeleteDialogVisible(true)}
        visible={false}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        id="cancelNotification"
        label={resourcesContext.messages['close']}
        onClick={onHideNotificationsList}
      />
    </div>
  );

  const downloadExportFMEFile = async notification => {
    try {
      const getFileName = () => {
        const extension = notification.content.fileName.split('.').pop();
        return `${notification.content.datasetName}.${extension}`;
      };

      let datasetData;

      if (notification) {
        if (notification.content.providerId) {
          const { data } = await DatasetService.downloadExportFile(
            notification.content.datasetId,
            notification.content.fileName,
            notification.content.providerId
          );
          datasetData = data;
        } else {
          const { data } = await DatasetService.downloadExportFile(
            notification.content.datasetId,
            notification.content.fileName
          );
          datasetData = data;
        }
        DownloadFile(datasetData, getFileName());
      }
    } catch (error) {
      console.error('NotificationsList - downloadExportFMEFile.', error);
      notificationContext.add({ type: 'DOWNLOAD_FME_FILE_ERROR' }, true);
    } finally {
      notificationContext.clearHiddenNotifications();
    }
  };

  const onLoadNotifications = async (fRow, nRows) => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await NotificationService.all({
        pageNum: Math.floor(fRow / nRows),
        pageSize: nRows
      });

      const parsedNotifications = unparsedNotifications.userNotifications.map(notification => {
        return NotificationService.parse({
          config: config.notifications.notificationSchema,
          content: notification.content,
          date: notification.date,
          message: resourcesContext.messages[camelCase(notification.type)],
          onClick:
            notification.type === 'EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT' ||
            notification.type === 'EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT'
              ? () => downloadExportFMEFile(notification)
              : null,
          routes,
          type: notification.type
        });
      });

      const notificationsArray = parsedNotifications.map((notification, i) => {
        const capitalizedLevelError = !isUndefined(notification.type)
          ? notification.type.charAt(0).toUpperCase() + notification.type.slice(1)
          : notification.type;

        return {
          index: i + nRows * Math.floor(fRow / nRows),
          key: notification.key,
          message: notification.message,
          levelError: capitalizedLevelError,
          date: getDateTimeFormatByUserPreferences(notification.date),

          downloadButton: notification.onClick ? (
            <span className={styles.center}>
              <Button
                className={`${styles.columnActionButton}`}
                icon="export"
                label={resourcesContext.messages['downloadFile']}
                onClick={() => notification.onClick()}
              />
            </span>
          ) : (
            ''
          ),
          redirectionUrl: !isNil(notification.redirectionUrl)
            ? `${window.location.protocol}//${window.location.hostname}${
                window.location.port !== '' && window.location.port.toString() !== '80'
                  ? `:${window.location.port}`
                  : ''
              }${notification.redirectionUrl}`
            : ''
        };
      });

      setTotalRecords(unparsedNotifications.totalRecords);
      setNotifications(notificationsArray);
    } catch (error) {
      console.error('NotificationsList - onLoadNotifications.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderNotifications = () => {
    if (notifications.length > 0) {
      return (
        <DataTable
          autoLayout={true}
          first={paginationInfo.firstPageRecord}
          hasDefaultCurrentPage={true}
          lazy={true}
          loading={isLoading}
          onPage={onChangePage}
          paginator={true}
          paginatorRight={<span>{`${resourcesContext.messages['totalRecords']} ${totalRecords}`}</span>}
          rowClassName={newNotificationsClassName}
          rows={paginationInfo.recordsPerPage}
          rowsPerPageOptions={[5, 10, 20]}
          summary="notificationsList"
          totalRecords={totalRecords}
          value={notifications}>
          {columns}
        </DataTable>
      );
    } else if (isLoading) {
      return (
        <div className={styles.loadingSpinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    } else {
      return (
        <div className={styles.notificationsWithoutTable}>
          <div className={styles.noNotifications}>{resourcesContext.messages['noNotifications']}</div>
        </div>
      );
    }
  };

  const newNotificationsClassName = rowData => ({
    'p-highlight-bg': rowData.index < notificationContext.all.filter(notification => !notification.isSystem).length
  });

  const onDelete = async () => {
    try {
      setIsDeleting(true);
      await notificationContext.deleteAll();
      await NotificationService.deleteAll();
    } catch (error) {
      console.error('NotificationsList - onDelete.', error);
      notificationContext.add({ type: 'DELETE_USER_NOTIFICATIONS_ERROR' }, true);
    } finally {
      onLoadNotifications();
      setIsDeleteDialogVisible(false);
      setIsDeleting(false);
    }
  };

  const renderNotificationsListContent = () => {
    if (isNotificationVisible) {
      return (
        <Fragment>
          <Dialog
            blockScroll={false}
            className="edit-table"
            contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
            footer={notificationsFooter}
            header={resourcesContext.messages['notifications']}
            modal={true}
            onHide={onHideNotificationsList}
            style={{ width: '80%' }}
            visible={isNotificationVisible}>
            {renderNotifications()}
          </Dialog>
          {isDeleteDialogVisible && (
            <ConfirmDialog
              classNameConfirm="p-button-danger"
              disabledConfirm={isDeleting}
              header={resourcesContext.messages['deleteUsersNotificationsHeader']}
              iconConfirm={isDeleting ? 'spinnerAnimate' : 'check'}
              labelCancel={resourcesContext.messages['no']}
              labelConfirm={resourcesContext.messages['yes']}
              onConfirm={onDelete}
              onHide={() => setIsDeleteDialogVisible(false)}
              visible={isDeleteDialogVisible}>
              {resourcesContext.messages['deleteUsersNotificationsConfirm']}
            </ConfirmDialog>
          )}
        </Fragment>
      );
    }
  };

  return renderNotificationsListContent();
};
