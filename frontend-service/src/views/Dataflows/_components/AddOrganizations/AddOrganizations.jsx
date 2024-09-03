import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import { Column } from 'primereact/column';

import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import styles from './AddOrganizations.module.scss';

import { config } from 'conf';

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

export const AddOrganizations = ({ isDialogVisible, onCloseDialog }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('addOrganizations'));

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [filteredRecords, setFilteredRecords] = useState(0);
  const [group, setGroup] = useState();
  const [isFiltered, setIsFiltered] = useState(false);
  const [isAddOrganizationDialogVisible, setIsAddOrganizationDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingButton, setIsLoadingButton] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [organizationName, setOrganizationName] = useState();
  const [organizationsList, setOrganizationsList] = useState([]);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [sort, setSort] = useState({ field: 'label', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);
  const [organizationCode, setOrganizationCode] = useState('');
  const { setData } = useApplyFilters('addOrganizations');

  const { firstRow, numberRows, pageNum } = pagination;

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    getOrganizations();
  }, [pagination, sort]);

  useInputTextFocus(isAddOrganizationDialogVisible, inputRef);

  const isValidOrganizationName = () => RegularExpressions['nonSymbols'].test(organizationName);
  const isValidOrganizationCode = () => RegularExpressions['nonSymbols'].test(organizationCode);

  const getOrganizations = async () => {
    setLoadingStatus('pending');

    try {
      const data = await AddOrganizationsService.getOrganizations({
        pageNum,
        numberRows,
        sortOrder: sort.order,
        sortField: sort.field,
        providerCode: filterBy.code,
        groupId: !filterBy.groupId ? filterBy.groupId : filterBy.groupId.value,
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

  const onSort = event => setSort({ field: event.sortField, order: event.sortOrder });

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
      dropdownOptions: [
        {
          label: resourcesContext.messages[config.providerGroup.EEA_MEMBER_COUNTRIES.label],
          value: 1
        },
        {
          label: resourcesContext.messages[config.providerGroup.ALL_COUNTRIES.label],
          value: 2
        },
        {
          label: resourcesContext.messages[config.providerGroup.MAP_MY_TREE_PROVIDERS.label],
          value: 3
        },
        {
          label: resourcesContext.messages[config.providerGroup.COMPANY_GROUP_1.label],
          value: 4
        },
        {
          label: resourcesContext.messages[config.providerGroup.LDV_MANUFACTURERS.label],
          value: 5
        },
        {
          label: resourcesContext.messages[config.providerGroup.COUNTRIES.label],
          value: 6
        }
      ],
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
    let groupKey;

    if (window.location.href.indexOf('sanbox') !== -1 || window.location.href.indexOf('dev') !== -1) {
      groupKey = Object.keys(config.providerGroupDev)[provider.groupId - 1];
      return groupKey ? <p>{resourcesContext.messages[config.providerGroupDev[groupKey].label]}</p> : '';
    } else if (window.location.href.indexOf('test') !== -1) {
      groupKey = Object.keys(config.providerGroupTest)[provider.groupId - 1];
      return groupKey ? <p>{resourcesContext.messages[config.providerGroupTest[groupKey].label]}</p> : '';
    } else {
      groupKey = Object.keys(config.providerGroup)[provider.groupId - 1];
      return groupKey ? <p>{resourcesContext.messages[config.providerGroup[groupKey].label]}</p> : '';
    }
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

  const hasEmptyData = () => isEmpty(organizationName) || isEmpty(group);

  const onRefresh = () => {
    setIsRefreshing(true);
    getOrganizations();
  };

  const onResetAll = () => {
    setIsRefreshing(true);
    setOrganizationName(null);
    setOrganizationCode('');
    setGroup(null);

    getOrganizations();
  };

  const onCloseAddDialog = () => {
    setIsAddOrganizationDialogVisible(false);
  };

  const createNewProvider = async () => {
    setIsLoadingButton(true);
    try {
      await AddOrganizationsService.createProvider({
        group: group.label,
        label: organizationName,
        code: organizationCode,
        groupId: group.group
      });
      onCloseAddDialog();
    } catch (error) {
      console.error('AddOrganizations - createNewProvider.', error);
      setLoadingStatus('error');
      notificationContext.add({ type: 'CREATE_ORGANIZATION_ERROR' }, true);
    } finally {
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
    </Fragment>
  );
};
