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
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';
import { DataTable } from 'views/_components/DataTable';

export const AddOrganizations = ({ addOrganizationDialogFooter, isDialogVisible, onCloseDialog }) => {
  const filterBy = useRecoilValue(filterByCustomFilterStore('addOrganizations'));

  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [filteredRecords, setFilteredRecords] = useState(0);
  const [isFiltered, setIsFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');
  const [organizationsList, setOrganizationsList] = useState([]);
  const [pagination, setPagination] = useState({ firstRow: 0, numberRows: 10, pageNum: 0 });
  const [sort, setSort] = useState({ field: 'label', order: -1 });
  const [totalRecords, setTotalRecords] = useState(0);

  const { setData } = useApplyFilters('addOrganizations');

  const { firstRow, numberRows, pageNum } = pagination;

  useEffect(() => {
    getOrganizations();
  }, [pagination, sort]);

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
          value: config.providerGroup.EEA_MEMBER_COUNTRIES.key
        },
        {
          type: resourcesContext.messages[config.providerGroup.ALL_COUNTRIES.label],
          value: config.providerGroup.ALL_COUNTRIES.key
        },
        {
          type: resourcesContext.messages[config.providerGroup.MAP_MY_TREE_PROVIDERS.label],
          value: config.providerGroup.MAP_MY_TREE_PROVIDERS.key
        },
        {
          type: resourcesContext.messages[config.providerGroup.COMPANY_GROUP_1.label],
          value: config.providerGroup.COMPANY_GROUP_1.key
        },
        {
          type: resourcesContext.messages[config.providerGroup.LDV_MANUFACTURERS.label],
          value: config.providerGroup.LDV_MANUFACTURERS.key
        },
        {
          type: resourcesContext.messages[config.providerGroup.COUNTRIES.label],
          value: config.providerGroup.COUNTRIES.key
        }
      ],
      template: 'groupId',
      type: 'MULTI_SELECT'
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
        key: 'group',
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
        expander={column.key === 'expanderColumn'}
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'buttonsUniqueId' && column.key !== 'expanderColumn'}
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

  const onRefresh = () => {
    setIsRefreshing(true);
    getOrganizations();
  };

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
            <p>{resourcesContext.messages['jobsStatusesNotMatchingFilter']}</p>
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
    <Dialog
      blockScroll={false}
      className="responsiveDialog"
      footer={addOrganizationDialogFooter()}
      header={resourcesContext.messages['addOrganization']}
      modal={true}
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      {renderDialogContent()}
    </Dialog>
  );
};
