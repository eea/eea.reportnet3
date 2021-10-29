import { useContext, useEffect, useState } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';
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
import { Spinner } from 'views/_components/Spinner';

import { NotificationService } from 'services/NotificationService';

// import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { isEmpty } from 'lodash';

const NotificationsList = ({ isNotificationVisible, setIsNotificationVisible }) => {
  // const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [columns, setColumns] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [paginationInfo, setPaginationInfo] = useState({
    totalRecords: 0,
    recordsPerPage: userContext.userProps.rowsPerPage,
    firstPageRecord: 0
  });
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
  }, [userContext]);

  useEffect(() => {
    if (!isEmpty(columns)) {
      onLoadNotifications(0, paginationInfo.recordsPerPage);
    }
  }, [columns]);

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

  const onChangePage = event => {
    setPaginationInfo({ ...paginationInfo, recordsPerPage: event.rows, firstPageRecord: event.first });
    onLoadNotifications(event.first, event.rows);
  };

  const onLoadNotifications = async (fRow, nRows) => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await NotificationService.all({
        pageNum: Math.floor(fRow / nRows),
        pageSize: nRows
      });
      console.log({ unparsedNotifications });
      const parsedNotifications = unparsedNotifications.userNotifications.map(notification => {
        return NotificationService.parse({
          config: config.notifications.notificationSchema,
          content: notification.content,
          message: resourcesContext.messages[camelCase(notification.type)],
          routes,
          type: notification.type
        });
      });
      console.log({ parsedNotifications });
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

      console.log({ notificationsArray });
      setNotifications(notificationsArray);
    } catch (error) {
      console.error('NotificationsList - onLoadNotifications.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderNotifications = () => {
    if (isLoading) {
      return <Spinner />;
    } else if (notifications.length > 0) {
      return (
        <DataTable
          autoLayout={true}
          loading={isLoading}
          onPage={onChangePage}
          paginator={true}
          paginatorRight={<span>{`${resourcesContext.messages['totalRecords']}  ${paginationInfo.totalRecords}`}</span>}
          rows={paginationInfo.recordsPerPage}
          rowsPerPageOptions={[5, 10, 20]}
          summary="notificationsList"
          totalRecords={paginationInfo.totalRecords}
          value={notifications}>
          {columns}
        </DataTable>
      );
    } else {
      return (
        <div className={styles.notificationsWithoutTable}>
          <div className={styles.noNotifications}>{resourcesContext.messages['noNotifications']}</div>
        </div>
      );
    }
  };

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
        {renderNotifications()}
      </Dialog>
    )
  );
};

export { NotificationsList };
