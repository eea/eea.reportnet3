import React, { useState, useEffect, useContext } from 'react';
import moment from 'moment';

import isUndefined from 'lodash/isUndefined';

import sanitizeHtml from 'sanitize-html';

import styles from './NotificationsList.module.scss';

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
  const [numberRows, setNumberRows] = useState(0);
  const [firstRow, setFirstRow] = useState(0);
  const [sortField, setSortField] = useState('');
  const [sortOrder, setSortOrder] = useState(0);

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
        header: resources.messages['url']
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
      const message = sanitizeHtml(notification.message, {
        allowedTags: [],
        allowedAttributes: {
          a: []
        }
      });

      const capitalizedMessageLevel = !isUndefined(notification.type)
        ? notification.type.charAt(0).toUpperCase() + notification.type.slice(1)
        : notification.type;
      return {
        message: message,
        messageLevel: capitalizedMessageLevel,
        date: moment(notification.date).format(
          `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
            userContext.userProps.amPm24h ? '' : ' A'
          }`
        ),
        redirectionUrl: `${window.location.protocol}//${window.location.hostname}${
          window.location.port !== '' && window.location.port.toString() !== '80' ? `:${window.location.port}` : ''
        }${notification.redirectionUrl}`
      };
    });
    console.info('notifications: %o', notificationsArray);
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
    return (
      <a href={getValidUrl(rowData.redirectionUrl)} target="_blank" rel="noopener noreferrer">
        {rowData.redirectionUrl}
      </a>
    );
  };

  const onChangePage = event => {
    setNumberRows(event.rows);
    setFirstRow(event.first);
  };

  return (
    <Dialog
      className="edit-table"
      blockScroll={false}
      contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
      closeOnEscape={false}
      header={resources.messages['notifications']}
      modal={true}
      onHide={() => setIsNotificationVisible(false)}
      style={{ width: '60%' }}
      visible={isNotificationVisible}
      zIndex={3100}>
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
    </Dialog>
  );
};

export { NotificationsList };
