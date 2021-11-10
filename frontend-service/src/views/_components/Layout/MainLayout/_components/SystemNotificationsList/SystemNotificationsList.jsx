import { useContext, useEffect, useReducer, useState, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import styles from './SystemNotificationsList.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dialog } from 'views/_components/Dialog';
import { DataTable } from 'views/_components/DataTable';
import { Dropdown } from 'views/_components/Dropdown';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { LevelError } from 'views/_components/LevelError';
import { Spinner } from 'views/_components/Spinner';
import { SystemNotificationsCreateForm } from './_components/SystemNotificationsCreateForm';
import { SystemNotificationFieldEditor } from './_components/SystemNotificationFieldEditor';

import { SystemNotificationService } from 'services/SystemNotificationService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { systemNotificationReducer } from './_functions/Reducers/systemNotificationReducer';

const SystemNotificationsList = ({ isSystemNotificationVisible, setIsSystemNotificationVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [systemNotificationState, dispatchSystemNotification] = useReducer(systemNotificationReducer, {
    editNotification: {},
    editingRows: [],
    formType: '',
    isVisibleCreateSysNotification: false,
    quickEditedNotification: {},
    systemNotifications: []
  });

  const {
    isVisibleCreateSysNotification,
    editingRows,
    editNotification,
    formType,
    quickEditedNotification,
    systemNotifications
  } = systemNotificationState;

  const [columns, setColumns] = useState([]);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedRow, setSelectedRow] = useState({});
  // const [isUpdating, setIsUpdating] = useState(false);

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
        template: rowData => notificationLevelTemplate(rowData, false)
      },
      {
        id: 'enabled',
        header: resourcesContext.messages['ruleEnabled'],
        template: enabledTemplate
      }
    ];

    let columnsArray = headers.map(col => (
      <Column
        body={col.template}
        className={col.className}
        editor={getEditor(col.id)}
        field={col.id}
        header={col.header}
        key={col.id}
        sortable={true}
      />
    ));

    columnsArray.push(
      <Column
        body={actionsColumnButtons}
        header={resourcesContext.messages['actions']}
        // className={styles.crudColumn}
        key="buttonsUniqueId"
        rowEditor={true}
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
          // isUpdating={isUpdating}
          onDeleteClick={() => setIsDeleteDialogVisible(true)}
          onEditClick={() => onEditClick(rowData)}
          rowDataId={rowData.key}
          rowDeletingId={rowData.key}
          rowUpdatingId={rowData.key}
        />
      </div>
    );
  };

  const getEditor = field => {
    switch (field) {
      case 'enabled':
        return row => checkboxEditor(row, 'enabled');
      case 'message':
        return row => textEditor(row, 'message');
      case 'level':
        return row => dropdownEditor(row, 'level');
      default:
        break;
    }
  };

  const checkboxEditor = (props, field) => {
    return (
      <div className={styles.checkboxEditorWrapper}>
        <Checkbox
          checked={props.rowData[field]}
          className={styles.checkboxEditor}
          id={props.rowData[field]?.toString()}
          inputId={props.rowData[field]?.toString()}
          onChange={e => onChange(props, e.checked)}
          role="checkbox"
        />
      </div>
    );
  };

  const dropdownEditor = (props, field) => {
    return (
      <Dropdown
        appendTo={document.body}
        filterPlaceholder={resourcesContext.messages['errorTypePlaceholder']}
        id="errorType"
        itemTemplate={rowData => notificationLevelTemplate(rowData, true)}
        onChange={e => onChange(props, e.target.value.value)}
        optionLabel="label"
        optionValue="value"
        options={config.validations.errorLevels}
        placeholder={resourcesContext.messages['errorTypePlaceholder']}
        value={{ label: props.rowData[field], value: props.rowData[field] }}
      />
    );
  };

  const textEditor = (props, field) => (
    <SystemNotificationFieldEditor
      initialValue={props.rowData[field]}
      keyfilter={['message'].includes(field) ? 'noDoubleQuote' : ''}
      onSaveField={onChange}
      required={['message'].includes(field)}
      systemNotifications={props}
    />
  );

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

  const onChange = (props, value, isText = false) => {
    const inmSystemNotifications = [...systemNotifications];
    const inmEditingRows = [...editingRows];
    console.log({ inmSystemNotifications }, props);
    const sysNotifIdx = inmSystemNotifications.findIndex(sysNotif => sysNotif.id === props.rowData.id);
    const editIdx = inmEditingRows.findIndex(sysNotif => sysNotif.id === props.rowData.id);
    if (inmSystemNotifications[sysNotifIdx][props.field] !== value && editIdx !== -1) {
      inmSystemNotifications[sysNotifIdx][props.field] = isText ? value.trim() : value;
      inmEditingRows[editIdx][props.field] = isText ? value.trim() : value;

      dispatchSystemNotification({
        type: 'ON_QUICK_EDIT',
        payload: { sysNotif: inmSystemNotifications, editRows: inmEditingRows }
      });
    }
  };

  const onCreateSystemNotification = async systemNotification => {
    console.log(systemNotification);
    // try{

    // }
  };

  const onEditClick = rowData => {
    dispatchSystemNotification({ type: 'ON_EDIT', payload: rowData });
  };

  const onDelete = async id => {
    setIsDeleting(true);
    try {
      // await systemNotificationType.delete(id);
      // onLoadSystemNotifications();
    } catch (error) {
      console.error('SystemNotificationsList - onDelete.', error);
      // notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' });
    } finally {
      setIsDeleteDialogVisible(false);
      setIsDeleting(false);
    }
  };

  const onLoadSystemNotifications = async () => {
    try {
      setIsLoading(true);
      const unparsedNotifications = await SystemNotificationService.all();
      // const parsedNotifications = unparsedNotifications.map(notification => {
      //   return SystemNotificationService.parse({
      //     config: config.notifications.notificationSchema,
      //     content: notification.content,
      //     date: notification.date,
      //     message: resourcesContext.messages[camelCase(notification.type)],
      //     routes,
      //     type: notification.type
      //   });
      // });
      // const notificationsArray = parsedNotifications.map(notification => {
      //   const message = DOMPurify.sanitize(notification.message, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });

      //   const capitalizedLevelError = !isUndefined(notification.type)
      //     ? notification.type.charAt(0).toUpperCase() + notification.type.slice(1)
      //     : notification.type;

      //   return {
      //     key: notification.key,
      //     message: message,
      //     levelError: capitalizedLevelError,
      //     date: dayjs(notification.date).format(
      //       `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
      //         userContext.userProps.amPm24h ? '' : ' A'
      //       }`
      //     ),

      //     downloadButton: notification.onClick ? (
      //       <span className={styles.center}>
      //         <Button
      //           className={`${styles.columnActionButton}`}
      //           icon="export"
      //           label={resourcesContext.messages['downloadFile']}
      //           onClick={() => notification.onClick()}
      //         />
      //       </span>
      //     ) : (
      //       ''
      //     ),
      //     redirectionUrl: !isNil(notification.redirectionUrl)
      //       ? `${window.location.protocol}//${window.location.hostname}${
      //           window.location.port !== '' && window.location.port.toString() !== '80'
      //             ? `:${window.location.port}`
      //             : ''
      //         }${notification.redirectionUrl}`
      //       : ''
      //   };
      // });
      dispatchSystemNotification({ type: 'SET_SYSTEM_NOTIFICATIONS', payload: unparsedNotifications });
    } catch (error) {
      console.error('SystemNotificationsList - onLoadSystemNotifications.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onToggleCreateFormVisibility = visible =>
    dispatchSystemNotification({ type: 'ON_TOGGLE_CREATE_FORM_VISIBILITY', payload: visible });

  const renderSystemNotifications = () => {
    console.log(systemNotifications);
    if (isLoading) {
      return <Spinner />;
    } else if (systemNotifications.length > 0) {
      return (
        <DataTable
          autoLayout={true}
          editMode="row"
          loading={false}
          onRowClick={event => {
            console.log(event.data);
            setSelectedRow(event.data);
          }}
          paginator={true}
          paginatorRight={<span>{`${resourcesContext.messages['totalRecords']}  ${systemNotifications.length}`}</span>}
          quickEditRowInfo={{
            updatedRow: selectedRow.id,
            property: 'id',
            condition: isLoading,
            requiredFields: ['message', 'level']
          }}
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
