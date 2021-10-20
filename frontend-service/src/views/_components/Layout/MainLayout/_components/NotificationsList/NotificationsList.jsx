import { useContext, useEffect, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import DOMPurify from 'dompurify';

import styles from './NotificationsList.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
// import { Filters } from 'views/_components/Filters';
import { LevelError } from 'views/_components/LevelError';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [columns, setColumns] = useState([]);
  const [notifications, setNotifications] = useState([]);
  // const [filteredData, setFilteredData] = useState([]);
  // const [isFiltered, setIsFiltered] = useState(false);

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

    const notificationsArray = notificationContext.all.map(notification => {
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
              window.location.port !== '' && window.location.port.toString() !== '80' ? `:${window.location.port}` : ''
            }${notification.redirectionUrl}`
          : ''
      };
    });
    setNotifications(notificationsArray);
  }, [notificationContext, userContext]);

  // const filterOptions = [
  //   { type: 'input', properties: [{ name: 'message' }] },
  //   { type: 'date', properties: [{ name: 'date' }] },
  //   { type: 'multiselect', properties: [{ name: 'levelError' }] }
  // ];

  // const getFilteredState = value => setIsFiltered(value);

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

  // const onLoadFilteredData = data => {
  //   console.log({ data });
  //   setFilteredData(data);
  // };

  return (
    isNotificationVisible && (
      <Dialog
        blockScroll={false}
        className="edit-table"
        contentStyle={{ height: '50%', maxHeight: '80%', overflow: 'auto' }}
        header={resourcesContext.messages['notifications']}
        modal={true}
        onHide={() => setIsNotificationVisible(false)}
        style={{ width: '80%' }}
        visible={isNotificationVisible}
        zIndex={3100}>
        {/* <Filters
          appendTo={document.body}
          data={notifications || []}
          getFilteredData={onLoadFilteredData}
          getFilteredSearched={getFilteredState}
          options={filterOptions}
          // searchBy={['message', 'date', 'levelError']}
        /> */}
        {console.log(notifications)}
        {notificationContext.all.length > 0 ? (
          <DataTable
            autoLayout={true}
            loading={false}
            paginator={true}
            paginatorRight={<span>{`${resourcesContext.messages['totalRecords']}  ${notifications.length}`}</span>}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            summary="notificationsList"
            totalRecords={notifications.length}
            value={notifications}>
            {columns}
          </DataTable>
        ) : (
          <div className={styles.notificationsWithoutTable}>
            <div className={styles.noNotifications}>{resourcesContext.messages['noNotifications']}</div>
          </div>
        )}
      </Dialog>
    )
  );
};

export { NotificationsList };
