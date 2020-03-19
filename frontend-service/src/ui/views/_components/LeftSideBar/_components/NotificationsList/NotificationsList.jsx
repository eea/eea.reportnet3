import React, { useState, useEffect, useContext } from 'react';

import sanitizeHtml from 'sanitize-html';

import styles from './NotificationsList.module.scss';

import { Column } from 'primereact/column';
import { Dialog } from 'ui/views/_components/Dialog';
import { DataTable } from 'ui/views/_components/DataTable';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

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
      }
    ];
    let columnsArray = headers.map(col => <Column sortable={true} key={col.id} field={col.id} header={col.header} />);
    setColumns(columnsArray);
    const notificationsArray = notificationContext.all.map(notification => {
      const message = sanitizeHtml(notification.message, {
        allowedTags: [],
        allowedAttributes: {
          a: []
        }
      });

      return {
        message: message,
        messageLevel: notification.type
      };
    });
    console.info('notifications: %o', notificationsArray);
    setNotifications(notificationsArray);
  }, [notificationContext]);
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
