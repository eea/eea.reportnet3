import { useContext, useEffect, useReducer, useState, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './SystemNotificationsList.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { SystemNotificationsCreateForm } from './_components/SystemNotificationsCreateForm';

import { SystemNotificationService } from 'services/SystemNotificationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { systemNotificationReducer } from './_functions/Reducers/systemNotificationReducer';

const SystemNotificationsList = ({ isSystemNotificationVisible, setIsSystemNotificationVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const isAdmin = userContext.hasPermission([config.permissions.roles.ADMIN.key]);

  const [systemNotificationState, dispatchSystemNotification] = useReducer(systemNotificationReducer, {
    editNotification: {},
    firstRow: 0,
    formType: '',
    isDeleteDialogVisible: false,
    isDeleting: false,
    isVisibleCreateSysNotification: false,
    numberRows: 10,
    systemNotifications: []
  });

  const {
    editNotification,
    firstRow,
    formType,
    isDeleteDialogVisible,
    isDeleting,
    isVisibleCreateSysNotification,
    numberRows,
    systemNotifications
  } = systemNotificationState;

  const [columns, setColumns] = useState([]);
  const [isCreating, setCreating] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedRow, setSelectedRow] = useState({});

  useEffect(() => {
    const headers = [
      {
        id: 'id',
        header: resourcesContext.messages['id'],
        className: styles.invisibleHeader
      },
      {
        id: 'message',
        header: resourcesContext.messages['message']
      },
      {
        id: 'level',
        header: resourcesContext.messages['notificationLevel'],
        template: rowData => notificationLevelTemplate(rowData, false),
        style: { width: '6rem' }
      }
    ];

    let columnsArray = headers.map(col => (
      <Column
        body={col.template}
        className={col.className}
        field={col.id}
        header={col.header}
        key={col.id}
        sortable={true}
        style={col.style}
      />
    ));

    if (isAdmin) {
      columnsArray.push(
        <Column
          body={enabledTemplate}
          field="enabled"
          header={resourcesContext.messages['ruleEnabled']}
          key="enabled"
          sortable={true}
        />
      );
      columnsArray.push(
        <Column body={actionsColumnButtons} header={resourcesContext.messages['actions']} key="buttonsUniqueId" />
      );
    }

    setColumns(columnsArray);
  }, []);

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
          onDeleteClick={() => onToggleDeleteVisibility(true)}
          onEditClick={() => onEditClick(rowData)}
          rowDataId={rowData.key}
          rowDeletingId={rowData.key}
          rowUpdatingId={rowData.key}
        />
      </div>
    );
  };

  const enabledTemplate = rowData => (
    <div className={styles.enabledColumnWrapper}>
      {rowData.enabled ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const notificationLevelTemplate = (rowData, isDropdown = false) => (
    <div className={styles.notificationLevelTemplateWrapper}>
      <LevelError type={isDropdown ? rowData.value.toLowerCase() : rowData.level?.toLowerCase()} />
    </div>
  );

  const systemNotificationsFooter = (
    <div>
      <Button
        className="p-button-animated-blink"
        icon="add"
        id="createSystemNotification"
        label={resourcesContext.messages['add']}
        onClick={() => onToggleCreateFormVisibility(true)}
        visible={isAdmin}
      />
      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        id="cancelCreateSystemNotification"
        label={resourcesContext.messages['close']}
        onClick={() => setIsSystemNotificationVisible(false)}
      />
    </div>
  );

  const onCreateSystemNotification = async systemNotification => {
    try {
      setCreating(true);
      await SystemNotificationService.create({ ...systemNotification });
    } catch (error) {
      console.error('SystemNotificationsList - onCreateSystemNotification', error);
      notificationContext.add({ type: 'CREATE_SYSTEM_NOTIFICATION_ERROR' }, true);
    } finally {
      onToggleCreateFormVisibility(false);
      setCreating(false);
      onLoadSystemNotifications();
    }
  };

  const onEditClick = rowData => {
    dispatchSystemNotification({ type: 'ON_EDIT', payload: rowData });
  };

  const onUpdateSystemNotification = async systemNotification => {
    try {
      setCreating(true);
      await SystemNotificationService.update({ ...systemNotification });
    } catch (error) {
      console.error('SystemNotificationsList - onUpdateSystemNotification', error);
      notificationContext.add({ type: 'UPDATE_SYSTEM_NOTIFICATION_ERROR' }, true);
    } finally {
      onToggleCreateFormVisibility(false);
      setCreating(false);
      onLoadSystemNotifications();
    }
  };

  const onChangePage = event => dispatchSystemNotification({ type: 'ON_CHANGE_PAGE', payload: event });

  const onDelete = async () => {
    try {
      dispatchSystemNotification({ type: 'ON_DELETE_START' });
      await SystemNotificationService.delete(selectedRow.id);
    } catch (error) {
      console.error('SystemNotificationsList - onDelete.', error);
      notificationContext.add({ type: 'DELETE_SYSTEM_NOTIFICATION_ERROR' }, true);
    } finally {
      dispatchSystemNotification({ type: 'ON_DELETE_END' });
      onLoadSystemNotifications();
    }
  };

  const onLoadSystemNotifications = async () => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await SystemNotificationService.all();
      dispatchSystemNotification({ type: 'SET_SYSTEM_NOTIFICATIONS', payload: unparsedNotifications });
    } catch (error) {
      console.error('SystemNotificationsList - onLoadSystemNotifications.', error);
      notificationContext.add({ type: 'GET_SYSTEM_NOTIFICATIONS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const onToggleDeleteVisibility = visible =>
    dispatchSystemNotification({ type: 'ON_TOGGLE_DELETE_VISIBILITY', payload: visible });

  const onToggleCreateFormVisibility = visible =>
    dispatchSystemNotification({ type: 'ON_TOGGLE_CREATE_FORM_VISIBILITY', payload: visible });

  const renderSystemNotifications = () => {
    if (isLoading) {
      return <Spinner className={styles.spinner} />;
    } else if (systemNotifications.length > 0) {
      return (
        <DataTable
          autoLayout={true}
          first={firstRow}
          hasDefaultCurrentPage={true}
          loading={isLoading}
          onPage={onChangePage}
          onRowClick={event => setSelectedRow(event.data)}
          paginator={true}
          paginatorRight={<span>{`${resourcesContext.messages['totalRecords']}  ${systemNotifications.length}`}</span>}
          rows={numberRows}
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
          isCreating={isCreating}
          isVisible={isVisibleCreateSysNotification}
          notification={editNotification}
          onCreateSystemNotification={onCreateSystemNotification}
          onToggleVisibility={onToggleCreateFormVisibility}
          onUpdateSystemNotification={onUpdateSystemNotification}
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
          onHide={() => onToggleDeleteVisibility(false)}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['deleteSystemNotificationConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

export { SystemNotificationsList };
