import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import { Column } from 'primereact/column';

import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './AddOrganizations.module.scss';

import { AddOrganizationsService } from 'services/AddOrganizationsService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { Filters } from 'views/_components/Filters';
import { filterByCustomFilterStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';
import { useInputTextFocus } from 'views/_functions/Hooks/useInputTextFocus';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';
import { DataTable } from 'views/_components/DataTable';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { InputText } from 'views/_components/InputText';
import { Dropdown } from 'views/_components/Dropdown';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { ActionsColumn } from 'views/_components/ActionsColumn';

export const AddOrganizations = ({ isDialogVisible, onCloseDialog }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('addOrganizations'));

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [actionsButtons, setActionsButtons] = useState({
    id: null,
    isActionButtonsLoading: false,
    isActionButtonsEditing: false
  });
  const [filteredRecords, setFilteredRecords] = useState(0);
  const [selectedGroup, setSelectedGroup] = useState();
  const [isFiltered, setIsFiltered] = useState(false);
  const [isAddOrganizationDialogVisible, setIsAddOrganizationDialogVisible] = useState(false);
  const [isEditOrganizationDialogVisible, setIsEditOrganizationDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingButton, setIsLoadingButton] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [organizationName, setOrganizationName] = useState('');
  const [organizationsList, setOrganizationsList] = useState([]);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [providerEditing, setProviderEditing] = useState();
  const [providerGroupsList, setProviderGroupsList] = useState([]);
  const [sort, setSort] = useState({ field: 'label', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);
  const [organizationCode, setOrganizationCode] = useState('');
  const { setData } = useApplyFilters('addOrganizations');

  const { firstRow, numberRows, pageNum } = pagination;

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    getProviderGroups();
  }, []);

  useEffect(() => {
    getOrganizations();
  }, [pagination, sort]);

  useInputTextFocus(isAddOrganizationDialogVisible, inputRef);

  const isValidOrganizationName = () => RegularExpressions['nonSymbols'].test(organizationName);
  const isValidOrganizationCode = () => RegularExpressions['nonSymbols'].test(organizationCode);

  const getProviderGroups = async () => {
    setLoadingStatus('pending');

    try {
      const data = await AddOrganizationsService.getProviderGroups();

      setProviderGroupsList(data);
      setLoadingStatus('success');
    } catch (error) {
      console.error('AddOrganizations - getProviderGroups.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_ORGANIZATIONS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getOrganizations = async () => {
    setLoadingStatus('pending');

    try {
      const data = await AddOrganizationsService.getOrganizations({
        pageNum,
        numberRows,
        sortOrder: sort.order,
        sortField: sort.field,
        providerCode: filterBy.code,
        groupId: !filterBy.groupId ? filterBy.groupId : filterBy.groupId.dataProviderGroupId,
        label: filterBy.label
      });

      setIsFiltered(filterBy ? (filterBy.label || filterBy.code || filterBy.groupId ? true : false) : false);
      setTotalRecords(data.totalRecords);
      setFilteredRecords(data.filteredRecords);
      setOrganizationsList(data.providersList);
      setData(data.providersList);
      setLoadingStatus('success');
    } catch (error) {
      console.error('AddOrganizations - getOrganizations.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'GET_ORGANIZATIONS_ERROR' }, true);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const onEditProviderName = provider => {
    setProviderEditing(provider);
    setIsEditOrganizationDialogVisible(true);
  };

  const onSort = event => setSort({ field: event.sortField, order: event.sortOrder });

  const filterOptions = [
    {
      nestedOptions: [
        { key: 'label', label: resourcesContext.messages['label'] },
        { key: 'code', label: resourcesContext.messages['code'] }
      ],

      type: 'INPUT'
    },
    {
      key: 'groupId',
      label: resourcesContext.messages['group'],
      dropdownOptions: providerGroupsList,
      template: 'groupId',
      type: 'DROPDOWN'
    }
  ];

  const getTableColumns = () => {
    const columns = [
      {
        key: 'id',
        header: resourcesContext.messages['providerId'],
        template: getProviderIdTemplate,
        className: styles.smallColumn
      },
      {
        key: 'label',
        header: resourcesContext.messages['label'],
        template: getProviderLabelTemplate,
        className: styles.largeColumn
      },
      {
        key: 'code',
        header: resourcesContext.messages['code'],
        template: getProviderCodeTemplate,
        className: styles.largeColumn
      },
      {
        key: 'group_id',
        header: resourcesContext.messages['group'],
        template: getProviderGroupTemplate,
        className: styles.largeColumn
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate,
        className: styles.smallColumn
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'actions'}
        style={column.style}
      />
    ));
  };

  const getProviderIdTemplate = provider => <p>{provider.id}</p>;

  const getProviderLabelTemplate = provider => <p>{provider.label}</p>;

  const getProviderCodeTemplate = provider => <p>{provider.code}</p>;

  const getProviderGroupTemplate = provider => (
    <p>{providerGroupsList.find(group => provider.groupId === group.dataProviderGroupId).label}</p>
  );
  const getActionsTemplate = provider => {
    return (
      <ActionsColumn
        disabledButtons={isNil(provider.id) && actionsButtons.isActionButtonsLoading}
        isUpdating={actionsButtons.isActionButtonsEditing}
        onEditClick={() => {
          setActionsButtons({
            ...actionsButtons,
            id: provider.id
          });
          onEditProviderName(provider);
        }}
        rowDataId={provider.id}
        rowUpdatingId={actionsButtons.id}
      />
    );
  };

  const renderAddOrganizationForm = () => {
    const hasError = !isEmpty(organizationName) && (isRepeatedOrganization() || !isValidOrganizationName());
    const hasCodeError = !isEmpty(organizationCode) && !isValidOrganizationCode();

    return (
      <div className={styles.addDialog}>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="organizationNameInput">
            {resourcesContext.messages['organizationName']}
          </label>
          <InputText
            className={hasError ? styles.error : ''}
            id="organizationNameInput"
            onChange={event => setOrganizationName(event.target.value)}
            placeholder={resourcesContext.messages['organizationNameDots']}
            ref={inputRef}
            style={{ margin: '0.3rem 0' }}
            value={organizationName}
          />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="codeInput">
            {resourcesContext.messages['code']}
          </label>
          <InputText
            className={hasCodeError ? styles.error : ''}
            disabled={false}
            id="codeInput"
            onChange={event =>
              setOrganizationCode(event.target.value?.replaceAll(' ', '').substring(0, 20).toUpperCase())
            }
            placeholder={resourcesContext.messages['organizationCodeDots']}
            style={{ margin: '0.3rem 0' }}
            value={organizationCode}
          />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="groupsDropdown">
            {resourcesContext.messages['group']}
          </label>
          <Dropdown
            appendTo={document.body}
            id="groupsDropdown"
            onChange={event => setSelectedGroup(event.target.value)}
            onKeyPress={event => onEnterKey(event.key)}
            optionLabel="label"
            options={providerGroupsList}
            placeholder={resourcesContext.messages['selectGroupOfCompanies']}
            ref={dropdownRef}
            style={{ margin: '0.3rem 0' }}
            value={selectedGroup}
          />
        </div>
      </div>
    );
  };

  const renderEditOrganizationForm = () => {
    const hasError = !isEmpty(organizationName) && (isRepeatedOrganization() || !isValidOrganizationName());

    return (
      <div className={styles.addDialog}>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="organizationNameInput">
            {resourcesContext.messages['organizationName']}
          </label>
          <InputText
            className={hasError ? styles.error : ''}
            id="organizationNameInput"
            onChange={event => {
              setOrganizationName(event.target.value);
              if (providerEditing.label !== '') {
                setProviderEditing({ ...providerEditing, label: '' });
              }
            }}
            placeholder={resourcesContext.messages['organizationNameDots']}
            ref={inputRef}
            style={{ margin: '0.3rem 0' }}
            value={organizationName ? organizationName : providerEditing.label}
          />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="codeInput">
            {resourcesContext.messages['code']}
          </label>
          <InputText disabled={true} style={{ margin: '0.3rem 0' }} value={providerEditing.code} />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="groupsDropdown">
            {resourcesContext.messages['group']}
          </label>
          <InputText
            disabled={true}
            style={{ margin: '0.3rem 0' }}
            value={providerGroupsList.find(group => providerEditing.groupId === group.dataProviderGroupId).label}
          />
        </div>
      </div>
    );
  };

  const hasEmptyData = () => isEmpty(organizationName) || isEmpty(selectedGroup);

  const onRefresh = () => {
    setIsRefreshing(true);
    getOrganizations();
  };

  const onResetAll = () => {
    setIsRefreshing(true);
    setOrganizationName('');
    setSelectedGroup(null);

    getOrganizations();
  };

  const onCloseAddDialog = () => {
    setIsAddOrganizationDialogVisible(false);
  };

  const onCloseEditDialog = () => {
    setIsEditOrganizationDialogVisible(false);
  };

  const createNewProvider = async () => {
    setIsLoadingButton(true);
    try {
      await AddOrganizationsService.createProvider({
        group: selectedGroup.label,
        label: organizationName,
        code: organizationCode,
        groupId: selectedGroup.dataProviderGroupId
      });
      onCloseAddDialog();
    } catch (error) {
      console.error('AddOrganizations - createNewProvider.', error);
      notificationContext.add(
        {
          type: 'CREATE_ORGANIZATION_ERROR',
          content: { customContent: { createError: error.response.data.message } }
        },
        true
      );
    } finally {
      onResetAll();
      setIsLoadingButton(false);
    }
  };

  const updateProvider = async () => {
    setIsLoadingButton(true);
    try {
      await AddOrganizationsService.updateProvider({
        id: actionsButtons.id,
        label: organizationName
      });
      onCloseEditDialog();
    } catch (error) {
      console.error('AddOrganizations - updateProvider.', error);
      setLoadingStatus('error');
      notificationContext.add(
        {
          type: 'UPDATE_ORGANIZATION_ERROR',
          content: { customContent: { updateError: error.response.data.message } }
        },
        true
      );
    } finally {
      onResetAll();
      setIsLoadingButton(false);
      onResetAll();
    }
  };

  const onEnterKey = key => {
    if (key === 'Enter') {
      createNewProvider();
    }
  };

  const isRepeatedOrganization = () => {
    const sameOrganizations = organizationsList.filter(organization =>
      TextUtils.areEquals(organization.label, organizationName)
    );
    return sameOrganizations.length > 0;
  };

  const dialogFooter = (
    <div className={styles.footer}>
      <Button
        className={`${styles.buttonLeft} p-button-animated-blink`}
        icon="plus"
        label={resourcesContext.messages['add']}
        onClick={() => setIsAddOrganizationDialogVisible(true)}
      />

      <Button
        className={`p-button-secondary ${styles.buttonPushRight}`}
        disabled={loadingStatus === 'pending'}
        icon={isRefreshing ? 'spinnerAnimate' : 'refresh'}
        label={resourcesContext.messages['refresh']}
        onClick={onRefresh}
      />
    </div>
  );

  const renderFilters = () => (
    <Filters
      className="lineItems"
      isLoading={loadingStatus === 'pending'}
      onFilter={() => setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 })}
      onReset={() => setPagination({ firstRow: 0, numberRows: pagination.numberRows, pageNum: 0 })}
      options={filterOptions}
      recoilId="addOrganizations"
    />
  );

  const getTooltipMessage = () => {
    if (hasEmptyData()) {
      return resourcesContext.messages['incompleteDataTooltip'];
    } else if (isRepeatedOrganization()) {
      return resourcesContext.messages['organizationAlreadyExists'];
    } else if (!isValidOrganizationName()) {
      return resourcesContext.messages['notValidOrganizationNameTooltip'];
    } else {
      return null;
    }
  };

  const getEditTooltipMessage = () => {
    if (isEmpty(organizationName)) {
      return resourcesContext.messages['incompleteDataTooltip'];
    } else if (isRepeatedOrganization()) {
      return resourcesContext.messages['organizationAlreadyExists'];
    } else if (!isValidOrganizationName()) {
      return resourcesContext.messages['notValidOrganizationNameTooltip'];
    } else {
      return null;
    }
  };

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isFiltered && isEmpty(organizationsList)) {
      return (
        <div className={styles.dialogContent}>
          {renderFilters()}
          <div className={styles.noDataContent}>
            <p>{resourcesContext.messages['addOrganizationsNotMatchingFilter']}</p>
          </div>
        </div>
      );
    }

    if (isEmpty(organizationsList)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <div className={styles.dialogContent}>
        {renderFilters()}
        <DataTable
          autoLayout={true}
          className={styles.jobStatusesTable}
          first={firstRow}
          hasDefaultCurrentPage={true}
          lazy={true}
          loading={loadingStatus === 'pending'}
          onPage={event =>
            setPagination({
              firstRow: event.first,
              numberRows: event.rows,
              pageNum: event.page
            })
          }
          onSort={onSort}
          paginator={true}
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={totalRecords}
              filteredDataLength={filteredRecords}
              isFiltered={isFiltered}
            />
          }
          reorderableColumns={true}
          resizableColumns={true}
          rows={numberRows}
          rowsPerPageOptions={[5, 10, 15]}
          sortField={sort.field}
          sortOrder={sort.order}
          totalRecords={isFiltered ? filteredRecords : totalRecords}
          value={organizationsList}>
          {getTableColumns()}
        </DataTable>
      </div>
    );
  };

  return (
    <Fragment>
      <Dialog
        blockScroll={false}
        className="responsiveDialog"
        footer={dialogFooter}
        header={resourcesContext.messages['addOrganization']}
        modal={true}
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isAddOrganizationDialogVisible && (
        <ConfirmDialog
          confirmTooltip={getTooltipMessage()}
          dialogStyle={{ minWidth: '400px', maxWidth: '600px' }}
          disabledConfirm={
            hasEmptyData() ||
            isLoadingButton ||
            isRepeatedOrganization() ||
            !isValidOrganizationName() ||
            !isValidOrganizationCode()
          }
          header={resourcesContext.messages['addOrganization']}
          iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={createNewProvider}
          onHide={() => {
            onResetAll();
            onCloseAddDialog();
          }}
          visible={isAddOrganizationDialogVisible}>
          {renderAddOrganizationForm()}
        </ConfirmDialog>
      )}

      {isEditOrganizationDialogVisible && (
        <ConfirmDialog
          confirmTooltip={getEditTooltipMessage()}
          dialogStyle={{ minWidth: '400px', maxWidth: '600px' }}
          disabledConfirm={
            isEmpty(organizationName) || isLoadingButton || isRepeatedOrganization() || !isValidOrganizationName()
          }
          header={resourcesContext.messages['edit']}
          iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={updateProvider}
          onHide={() => {
            onResetAll();
            onCloseEditDialog();
          }}
          visible={isEditOrganizationDialogVisible}>
          {renderEditOrganizationForm()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
