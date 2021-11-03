import { useContext, useEffect, useReducer, useState, Fragment } from 'react';

import { config } from 'conf';
import { routes } from 'conf/routes';

import camelCase from 'lodash/camelCase';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import DOMPurify from 'dompurify';

import styles from './SystemNotificationsList.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { SystemNotificationsCreateForm } from './_components/SystemNotificationsCreateForm';

import { NotificationService } from 'services/NotificationService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { systemNotificationReducer } from './_functions/Reducers/systemNotificationReducer';

const SystemNotificationsList = ({ isSystemNotificationVisible, setIsSystemNotificationVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [systemNotificationState, dispatchSystemNotification] = useReducer(systemNotificationReducer, {
    isVisibleCreateSysNotification: false,
    editNotification: {},
    formType: ''
  });

  const { isVisibleCreateSysNotification, editNotification, formType } = systemNotificationState;

  const [columns, setColumns] = useState([]);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [systemNotifications, setSystemNotifications] = useState([]);

  useEffect(() => {
    const headers = [
      {
        id: 'key',
        header: `${resourcesContext.messages['type']} (${resourcesContext.messages['key']})`
      },
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

    columnsArray.push(
      <Column
        body={actionsColumnButtons}
        // className={styles.crudColumn}
        header={resourcesContext.messages['actions']}
        key="buttonsUniqueId"
      />
    );

    setColumns(columnsArray);
  }, [userContext]);

  useEffect(() => {
    if (!isEmpty(columns)) {
      onLoadSystemNotifications();
    }
  }, [columns]);

  const actionsColumnButtons = rowData => {
    return (
      <div className={styles.actionsColumnButtons}>
        <ActionsColumn
          isDeletingDocument={isDeleting}
          isUpdating={isUpdating}
          onDeleteClick={() => setIsDeleteDialogVisible(true)}
          onEditClick={() => onEditClick(rowData)}
          rowDataId={rowData.key}
          rowDeletingId={rowData.key}
          rowUpdatingId={rowData.key}
        />
      </div>
    );
  };

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
      <LevelError type={rowData.levelError?.toLowerCase()} />
    </div>
  );

  const systemNotificationsFooter = (
    <div>
      <Button
        className="p-button-animated-blink"
        icon="add"
        id="createSystemNotification"
        // className={`${styles.columnActionButton}`}
        label={resourcesContext.messages['add']}
        onClick={() => onToggleCreateFormVisibility(true)}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        id="cancelCreateSystemNotification"
        label={resourcesContext.messages['cancel']}
        onClick={() => setIsSystemNotificationVisible(false)}
      />
    </div>
  );

  const onCreateSystemNotification = async systemNotification => {
    // console.log('', systemNotification);
  };

  const onDelete = async () => {
    setIsDeleting(true);
    try {
      // await NotificationService.delete();
      // onLoadSystemNotifications();
    } catch (error) {
      console.error('SystemNotificationsList - onDelete.', error);
      // notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' });
    } finally {
      setIsDeleteDialogVisible(false);
      setIsDeleting(false);
    }
  };

  const onEditClick = rowData => {
    dispatchSystemNotification({ type: 'ON_EDIT', payload: rowData });
  };

  const onLoadSystemNotifications = async () => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await NotificationService.all();
      const parsedNotifications = unparsedNotifications.map(notification => {
        return NotificationService.parse({
          config: config.notifications.notificationSchema,
          content: notification.content,
          date: notification.date,
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
          key: notification.key,
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

      setSystemNotifications(notificationsArray);
    } catch (error) {
      console.error('SystemNotificationsList - onLoadSystemNotifications.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onToggleCreateFormVisibility = visible =>
    dispatchSystemNotification({ type: 'ON_TOGGLE_CREATE_FORM_VISIBILITY', payload: visible });

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
      {isSystemNotificationVisible && (
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
      )}
      {isVisibleCreateSysNotification && (
        <SystemNotificationsCreateForm
          formType={formType}
          isVisible={isVisibleCreateSysNotification}
          notification={editNotification}
          onCreateSystemNotification={onCreateSystemNotification}
          onToggleVisibility={onToggleCreateFormVisibility}
        />
      )}
      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          disabledConfirm={isDeleting}
          header={resourcesContext.messages['deleteSystemNotificationHeader']}
          iconConfirm={isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDelete()}
          onHide={() => setIsDeleteDialogVisible(false)}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['deleteSystemNotificationConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

export { SystemNotificationsList };
