import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import { Column } from 'primereact/column';

import { useRecoilValue } from 'recoil';

import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import styles from './AddOrganizations.module.scss';

import { config } from 'conf';

import { AddOrganizationsService } from 'services/AddOrganizationsService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { Filters } from 'views/_components/Filters';
import { filterByCustomFilterStore } from 'views/_components/Filters/_functions/Stores/filterStore';
import { FiltersUtils } from 'views/_components/Filters/_functions/Utils/FiltersUtils';
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

export const AddOrganizations = ({ isDialogVisible, onCloseDialog }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('addOrganizations'));

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [filteredRecords, setFilteredRecords] = useState(0);
  const [group, setGroup] = useState();
  const [isFiltered, setIsFiltered] = useState(false);
  const [isAddOrganizationDialogVisible, setIsAddOrganizationDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [organizationsList, setOrganizationsList] = useState([]);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [organizationName, setOrganizationName] = useState();
  const [sort, setSort] = useState({ field: 'label', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);

  const { setData } = useApplyFilters('addOrganizations');

  const { firstRow, numberRows, pageNum } = pagination;

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  console.log(group);
  useEffect(() => {
    getOrganizations();
  }, [pagination, sort]);

  useInputTextFocus(isAddOrganizationDialogVisible, inputRef);

  const getOrganizations = async () => {
    setLoadingStatus('pending');

    try {
      const data = await AddOrganizationsService.getOrganizations({
        pageNum,
        numberRows,
        sortOrder: sort.order,
        sortField: sort.field,
        providerCode: filterBy.code,
        groupId: filterBy.groupId?.join(),
        label: filterBy.label
      });

      setIsFiltered(FiltersUtils.getIsFiltered(filterBy));
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
      multiSelectOptions: [
        {
          type: resourcesContext.messages[config.providerGroup.EEA_MEMBER_COUNTRIES.label],
          value: 1
        },
        {
          type: resourcesContext.messages[config.providerGroup.ALL_COUNTRIES.label],
          value: 2
        },
        {
          type: resourcesContext.messages[config.providerGroup.MAP_MY_TREE_PROVIDERS.label],
          value: 3
        },
        {
          type: resourcesContext.messages[config.providerGroup.COMPANY_GROUP_1.label],
          value: 4
        },
        {
          type: resourcesContext.messages[config.providerGroup.LDV_MANUFACTURERS.label],
          value: 5
        },
        {
          type: resourcesContext.messages[config.providerGroup.COUNTRIES.label],
          value: 6
        }
      ],
      template: 'groupId',
      type: 'MULTI_SELECT'
    }
  ];

  const groupOptions = [
    {
      label: resourcesContext.messages[config.providerGroup.EEA_MEMBER_COUNTRIES.label],
      group: 1
    },
    {
      label: resourcesContext.messages[config.providerGroup.ALL_COUNTRIES.label],
      group: 2
    },
    {
      label: resourcesContext.messages[config.providerGroup.MAP_MY_TREE_PROVIDERS.label],
      group: 3
    },
    {
      label: resourcesContext.messages[config.providerGroup.COMPANY_GROUP_1.label],
      group: 4
    },
    {
      label: resourcesContext.messages[config.providerGroup.LDV_MANUFACTURERS.label],
      group: 5
    },
    {
      label: resourcesContext.messages[config.providerGroup.COUNTRIES.label],
      group: 6
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
        className: styles.middleColumn
      },
      {
        key: 'code',
        header: resourcesContext.messages['code'],
        template: getProviderCodeTemplate,
        className: styles.middleColumn
      },
      {
        key: 'group_id',
        header: resourcesContext.messages['group'],
        template: getProviderGroupTemplate,
        className: styles.middleColumn
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
        sortable={column.key !== 'id'}
        style={column.style}
      />
    ));
  };

  const getProviderIdTemplate = provider => <p>{provider.id}</p>;

  const getProviderLabelTemplate = provider => <p>{provider.label}</p>;

  const getProviderCodeTemplate = provider => <p>{provider.code}</p>;

  const getProviderGroupTemplate = provider => {
    const groupKey = Object.keys(config.providerGroup)[provider.groupId - 1];

    return <p>{resourcesContext.messages[config.providerGroup[groupKey].label]}</p>;
  };

  const renderAddOrganizationForm = () => {
    return (
      <div className={styles.addDialog}>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="organizationNameInput">
            {resourcesContext.messages['organizationName']}
          </label>
          <InputText
            // className={hasError ? styles.error : ''}
            // disabled={!userRight.isNew}
            id="organizationNameInput"
            onChange={event => setOrganizationName(event.target.value)}
            placeholder={resourcesContext.messages['organizationNameDots']}
            ref={inputRef}
            style={{ margin: '0.3rem 0' }}
            value={organizationName}
          />
        </div>
        <div className={styles.inputWrapper}>
          <label className={styles.label} htmlFor="groupsDropdown">
            {resourcesContext.messages['group']}
          </label>
          <Dropdown
            appendTo={document.body}
            id="groupsDropdown"
            onChange={event => setGroup(event.target.value)}
            onKeyPress={event => onEnterKey(event.key)}
            optionLabel="label"
            options={groupOptions}
            placeholder={resourcesContext.messages['selectGroupOfCompanies']}
            ref={dropdownRef}
            style={{ margin: '0.3rem 0' }}
            value={group}
          />
        </div>
      </div>
    );
  };

  const onRefresh = () => {
    setIsRefreshing(true);
    getOrganizations();
  };

  const onResetAll = () => {
    setIsRefreshing(true);

    getOrganizations();
  };

  const onCloseAddDialog = () => {
    setIsAddOrganizationDialogVisible(false);
  };

  const createNewProvider = async () => {
    try {
      await AddOrganizationsService.createProvider({
        group: group.label,
        label: organizationName,
        code: organizationName,
        groupId: group.group
      });
    } catch (error) {
      console.error('AddOrganizations - createNewProvider.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'CREATE_ORGANIZATION_ERROR' }, true);
    }
  };

  const onEnterKey = key => {
    if (key === 'Enter') {
      createNewProvider();
    }
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
          // confirmTooltip={getTooltipMessage(userRight)}
          dialogStyle={{ minWidth: '400px', maxWidth: '600px' }}
          // disabledConfirm={
          //   hasEmptyData(userRight) ||
          //   isLoadingButton ||
          //   (!userRight.isNew && !isRoleChanged(userRight)) ||
          //   shareRightsState.accountHasError
          // }
          header={resourcesContext.messages['addOrganization']}
          // iconConfirm={isLoadingButton ? 'spinnerAnimate' : 'check'}
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
    </Fragment>
  );
};
