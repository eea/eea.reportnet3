import { useContext, useEffect, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import DOMPurify from 'dompurify';

import styles from './NotificationsList.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { Dialog } from 'ui/views/_components/Dialog';
import { DataTable } from 'ui/views/_components/DataTable';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [columns, setColumns] = useState([]);
  const [notifications, setNotifications] = useState([]);
  // const [numberRows, setNumberRows] = useState(0);
  // const [firstRow, setFirstRow] = useState(0);
  // const [sortField, setSortField] = useState('');
  // const [sortOrder, setSortOrder] = useState(0);

  useEffect(() => {
    const headers = [
      {
        id: 'message',
        header: resources.messages['message']
      },
      {
        id: 'messageLevel',
        header: resources.messages['notificationLevel']
      },
      {
        id: 'date',
        header: resources.messages['date']
      },
      {
        id: 'redirectionUrl',
        header: resources.messages['action']
      }
    ];

    let columnsArray = headers.map(col => (
      <Column
        body={col.id === 'redirectionUrl' ? linkTemplate : null}
        sortable={true}
        key={col.id}
        field={col.id}
        header={col.header}
      />
    ));

    setColumns(columnsArray);

    const notificationsArray = notificationContext.all.map(notification => {
      const message = DOMPurify.sanitize(notification.message, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });

      const capitalizedMessageLevel = !isUndefined(notification.type)
        ? notification.type.charAt(0).toUpperCase() + notification.type.slice(1)
        : notification.type;

      return {
        message: message,
        messageLevel: capitalizedMessageLevel,
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
              onClick={() => notification.onClick()}
              label={resources.messages['downloadFile']}
            />
          </span>
        ) : (
          ''
        ),
        redirectionUrl: !isNil(notification.redirectionUrl)
          ? `${window.location.protocol}//${window.location.hostname}${
              window.location.port !== '' && window.location.port.toString() !== '80' ? `:${window.location.port}` : ''
            }${notification.redirectionUrl}`
          : ''
      };
    });
    // console.info('notifications: %o', notificationsArray);
    setNotifications(notificationsArray);
  }, [notificationContext, userContext]);

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
        <a href={getValidUrl(rowData.redirectionUrl)} target="_self" rel="noopener noreferrer">
          {rowData.redirectionUrl}
        </a>
      )
    );
  };

  // const onChangePage = event => {
  //   setNumberRows(event.rows);
  //   setFirstRow(event.first);
  // };

  return (
    isNotificationVisible && (
      <Dialog
        className="edit-table"
        blockScroll={false}
        contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
        header={resources.messages['notifications']}
        modal={true}
        onHide={() => setIsNotificationVisible(false)}
        style={{ width: '60%' }}
        visible={isNotificationVisible}
        zIndex={3100}>
        {notificationContext.all.length > 0 ? (
          <DataTable
            autoLayout={true}
            loading={false}
            paginator={true}
            paginatorRight={<span>{`${resources.messages['totalRecords']}  ${notifications.length}`}</span>}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={notifications.length}
            value={notifications}>
            {columns}
          </DataTable>
        ) : (
          <div className={styles.notificationsWithoutTable}>
            <div className={styles.noNotifications}>{resources.messages['noNotifications']}</div>
          </div>
        )}
      </Dialog>
    )
  );
};

export { NotificationsList };
