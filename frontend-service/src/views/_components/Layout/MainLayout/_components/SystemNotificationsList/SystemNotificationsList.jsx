import { useContext, useEffect, useState, Fragment } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import DOMPurify from 'dompurify';

import styles from './SystemNotificationsList.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { SystemNotificationsCreateForm } from './_components/SystemNotificationsCreateForm';

import { NotificationService } from 'services/NotificationService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { isEmpty } from 'lodash';

const SystemNotificationsList = ({ isSystemNotificationVisible, setIsSystemNotificationVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [columns, setColumns] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isVisibleCreateSysNotification, setIsVisibleCreateSysNotification] = useState(false);
  const [systemNotifications, setSystemNotifications] = useState([]);

  useEffect(() => {
    const headers = [
      {
        id: 'message',
        header: resourcesContext.messages['message']
      },
      {
        id: 'levelError',
        header: resourcesContext.messages['notificationLevel'],
        template: notificationLevelTemplate
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
      <Column body={col.template} field={col.id} header={col.header} key={col.id} sortable={true} />
    ));

    setColumns(columnsArray);
  }, [userContext]);

  useEffect(() => {
    if (!isEmpty(columns)) {
      onLoadSystemNotifications();
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

  const notificationLevelTemplate = rowData => (
    <div className={styles.notificationLevelTemplateWrapper}>
      <LevelError type={rowData.levelError.toLowerCase()} />
    </div>
  );

  const systemNotificationsFooter = (
    <Button
      id="createSystemNotification"
      // className={`${styles.columnActionButton}`}
      icon="add"
      label={resourcesContext.messages['add']}
      onClick={() => setIsVisibleCreateSysNotification(true)}
    />
  );

  const onCreateSystemNotification = async systemNotification => {
    console.log('CREADA NOTIFICATION CON VALORES', systemNotification);
  };
  const onLoadSystemNotifications = async () => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await NotificationService.all();
      const parsedNotifications = unparsedNotifications.map(notification => {
        return NotificationService.parse({
          config: config.notifications.notificationSchema,
          content: notification.content,
          message: resourcesContext.messages[camelCase(notification.type)],
          routes,
          type: notification.type
        });
      });
      const notificationsArray = parsedNotifications.map(notification => {
        const message = DOMPurify.sanitize(notification.message, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });

        const capitalizedLevelError = !isUndefined(notification.type)
          ? notification.type.charAt(0).toUpperCase() + notification.type.slice(1)
          : notification.type;

        return {
          message: message,
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

      console.log({ notificationsArray });
      setSystemNotifications(notificationsArray);
    } catch (error) {
      console.error('SystemNotificationsList - onLoadSystemNotifications.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderSystemNotifications = () => {
    if (isLoading) {
      return <Spinner />;
    } else if (systemNotifications.length > 0) {
      return (
        <DataTable
          autoLayout={true}
          loading={false}
          paginator={true}
          paginatorRight={<span>{`${resourcesContext.messages['totalRecords']}  ${systemNotifications.length}`}</span>}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary="notificationsList"
          totalRecords={systemNotifications.length}
          value={systemNotifications}>
          {columns}
        </DataTable>
      );
    } else {
      return (
        <div className={styles.notificationsWithoutTable}>
          <div className={styles.noNotifications}>{resourcesContext.messages['noSystemNotifications']}</div>
        </div>
      );
    }
  };

  return (
    <Fragment>
      isSystemNotificationVisible && (
      <Dialog
        blockScroll={false}
        className="edit-table"
        contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
        footer={systemNotificationsFooter}
        header={resourcesContext.messages['systemNotifications']}
        modal={true}
        onHide={() => setIsSystemNotificationVisible(false)}
        style={{ width: '80%' }}
        visible={isSystemNotificationVisible}
        zIndex={3100}>
        {renderSystemNotifications()}
      </Dialog>
      ) isVisibleCreateSysNotification && (
      <SystemNotificationsCreateForm
        onCreateSystemNotification={onCreateSystemNotification}
        isSystemNotificationVisible={isSystemNotificationVisible}
        setIsSystemNotificationVisible={setIsSystemNotificationVisible}
      />
      )
    </Fragment>
  );
};

export { SystemNotificationsList };
