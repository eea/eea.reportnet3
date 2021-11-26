import { useContext, useEffect, useState } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './NotificationsList.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';

import { NotificationService } from 'services/NotificationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [columns, setColumns] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    recordsPerPage: userContext.userProps.rowsPerPage,
    firstPageRecord: 0
  });
  const [totalRecords, setTotalRecords] = useState(0);

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

    let columnsArray = headers.map(col => (
      <Column body={col.template} field={col.id} header={col.header} key={col.id} style={col.style} />
    ));

    setColumns(columnsArray);
  }, [userContext]);

  useEffect(() => {
    if (!isEmpty(columns)) {
      onLoadNotifications(0, paginationInfo.recordsPerPage);
    }
  }, [columns]);

  const getValidUrl = (url = '') => {
    let newUrl = window.decodeURIComponent(url);
    newUrl = newUrl.trim().replace(/\s/g, '');

    if (/^(:\/\/)/.test(newUrl)) return `http${newUrl}`;

    if (!/^(f|ht)tps?:\/\//i.test(newUrl)) return `//${newUrl}`;

    return newUrl;
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

  const notificationLevelTemplate = rowData => {
    return (
      !isNil(rowData.levelError) && (
        <div className={styles.notificationLevelTemplateWrapper}>
          <LevelError type={rowData.levelError.toLowerCase()} />
        </div>
      )
    );
  };

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
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        id="cancelNotification"
        label={resourcesContext.messages['close']}
        onClick={onHideNotificationsList}
      />
    </div>
  );

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
          date: dayjs(notification.date).format(
            `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
              userContext.userProps.amPm24h ? '' : ' A'
            }`
          ),

          downloadButton: notification.onClick ? (
            <span className={styles.center}>
              <Button
                className={`${styles.columnActionButton}`}
                icon={'export'}
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
    } else {
      if (isLoading) {
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
    }
  };

  const newNotificationsClassName = rowData => {
    return {
      'p-highlight-bg': rowData.index < notificationContext.all.filter(notification => !notification.isSystem).length
    };
  };

  const renderNotificationsListContent = () => {
    if (isNotificationVisible) {
      return (
        <Dialog
          blockScroll={false}
          className="edit-table"
          contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
          header={resourcesContext.messages['notifications']}
          modal={true}
          onHide={() => {
            setIsNotificationVisible(false);
            notificationContext.deleteAll();
          }}
          style={{ width: '80%' }}
          visible={isNotificationVisible}
          zIndex={3100}>
          {renderNotifications()}
        </Dialog>
      );
    }
  };

  return renderNotificationsListContent();
};

export { NotificationsList };
