import { Fragment, useContext, useEffect, useLayoutEffect, useState } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import orderBy from 'lodash/orderBy';
import pull from 'lodash/pull';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'views/_components/Filters';
import { ReferencedDataflowItem } from './_components/ReferencedDataflowItem';
import { Spinner } from 'views/_components/Spinner';

import { UserService } from 'services/UserService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { DataflowsListUtils } from './_functions/Utils/DataflowsListUtils';

const DataflowsList = ({ className, content = {}, isCustodian, isLoading, visibleTab }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataToFilter, setDataToFilter] = useState({
    reporting: content['reporting'],
    business: content['business'],
    reference: content['reference']
  });
  const [filteredData, setFilteredData] = useState(dataToFilter[visibleTab]);
  const [pinnedSeparatorIndex, setPinnedSeparatorIndex] = useState(-1);

  useLayoutEffect(() => {
    const parsedDataflows = orderBy(
      DataflowsListUtils.parseDataToFilter(content[visibleTab], userContext.userProps.pinnedDataflows),
      ['pinned', 'expirationDate', 'status', 'id'],
      ['asc', 'asc', 'asc', 'asc']
    );
    setDataToFilter({ ...dataToFilter, [visibleTab]: parsedDataflows });
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');

    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
  }, [content]);

  useEffect(() => {
    const parsedDataflows = orderBy(
      filteredData,
      ['pinned', 'expirationDate', 'status', 'id'],
      ['asc', 'asc', 'asc', 'asc']
    );
    const orderedPinned = parsedDataflows.map(el => el.pinned === 'pinned');
    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
  }, [filteredData]);

  const onLoadFilteredData = data => setFilteredData(data);

  const changeUserProperties = async userProperties => {
    try {
      return await UserService.updateConfiguration(userProperties);
    } catch (error) {
      console.error('DataflowsList - changeUserProperties.', error);
      notificationContext.add({ type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR' });
    }
  };

  const isFilteredByPinned = () =>
    filteredData.filter(dataflow => dataflow.pinned === 'pinned').length === filteredData.length ||
    filteredData.filter(dataflow => dataflow.pinned === 'unpinned').length === filteredData.length;

  const reorderDataflows = async (pinnedItem, isPinned) => {
    const inmUserProperties = { ...userContext.userProps };
    const inmPinnedDataflows = intersection(
      inmUserProperties.pinnedDataflows,
      [...dataToFilter.reporting, ...dataToFilter.reference, ...dataToFilter.business].map(data => data.id.toString())
    );
    if (!isEmpty(inmPinnedDataflows) && inmPinnedDataflows.includes(pinnedItem.id.toString())) {
      pull(inmPinnedDataflows, pinnedItem.id.toString());
    } else {
      inmPinnedDataflows.push(pinnedItem.id.toString());
    }
    inmUserProperties.pinnedDataflows = inmPinnedDataflows;
    await changeUserProperties(inmUserProperties);
    userContext.onChangePinnedDataflows(inmPinnedDataflows);

    const inmfilteredData = [...filteredData];
    const changedFilteredData = inmfilteredData.map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned ? 'pinned' : 'unpinned';
      }
      return item;
    });

    if (isPinned) {
      notificationContext.add({ type: 'DATAFLOW_PINNED_INIT' });
    } else {
      notificationContext.add({ type: 'DATAFLOW_UNPINNED_INIT' });
    }

    const orderedFilteredData = orderBy(
      changedFilteredData,
      ['pinned', 'expirationDate', 'status', 'id'],
      ['asc', 'asc', 'asc', 'asc']
    );

    const orderedPinned = orderedFilteredData.map(el => el.pinned);
    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));

    const inmDataToFilter = { ...dataToFilter };
    const changedInitialData = inmDataToFilter[visibleTab].map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned ? 'pinned' : 'unpinned';
      }
      return item;
    });

    setDataToFilter({
      ...dataToFilter,
      [visibleTab]: orderBy(
        changedInitialData,
        ['pinned', 'expirationDate', 'status', 'id'],
        ['asc', 'asc', 'asc', 'asc']
      )
    });
  };

  const filterOptions = {
    reporting: [
      {
        type: 'input',
        properties: [
          { name: 'name' },
          { name: 'description' },
          { name: 'legalInstrument' },
          { name: 'obligationTitle' },
          { name: 'obligationId' }
        ]
      },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }] },
      { type: 'date', properties: [{ name: 'expirationDate' }] }
    ],
    business: [
      {
        type: 'input',
        properties: [
          { name: 'name' },
          { name: 'description' },
          { name: 'legalInstrument' },
          { name: 'obligationTitle' },
          { name: 'obligationId' }
        ]
      },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }] },
      { type: 'date', properties: [{ name: 'expirationDate' }] }
    ],
    reference: [
      { type: 'input', properties: [{ name: 'name' }, { name: 'description' }] },
      { type: 'multiselect', properties: [{ name: 'status' }, { name: 'pinned' }] }
    ]
  };

  const renderDataflowItem = dataflow => {
    switch (visibleTab) {
      case 'reporting':
        return <DataflowsItem isCustodian={isCustodian} itemContent={dataflow} reorderDataflows={reorderDataflows} />;

      case 'business':
        return <DataflowsItem isCustodian={isCustodian} itemContent={dataflow} reorderDataflows={reorderDataflows} />;

      case 'reference':
        return <ReferencedDataflowItem dataflow={dataflow} reorderDataflows={reorderDataflows} />;

      default:
        break;
    }
  };

  const renderContent = () => {
    if (isLoading) return <Spinner style={{ top: 0 }} />;

    if (isEmpty(content[visibleTab])) {
      const emptyDataflowsMessage = {
        business: 'thereAreNoBusinessDataflows',
        reference: 'thereAreNoReferenceDataflows',
        reporting: 'thereAreNoReportingDataflows'
      };

      return <div className={styles.noDataflows}>{resources.messages[emptyDataflowsMessage[visibleTab]]}</div>;
    }

    return !isEmpty(filteredData) ? (
      filteredData.map((dataflow, i) => (
        <Fragment key={dataflow.id}>
          {renderDataflowItem(dataflow)}
          {!isFilteredByPinned() && pinnedSeparatorIndex === i ? <hr className={styles.pinnedSeparator} /> : null}
        </Fragment>
      ))
    ) : (
      <div className={styles.noDataflows}>{resources.messages['noDataflowsWithSelectedParameters']}</div>
    );
  };

  return (
    <div className={`${styles.wrap} ${className}`}>
      <div className="dataflowList-filters-help-step">
        {visibleTab === 'reporting' && (
          <Filters
            className={'dataflowsListFilters'}
            data={dataToFilter['reporting']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['reporting']}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}

        {visibleTab === 'business' && (
          <Filters
            className={'dataflowsListFilters'}
            data={dataToFilter['business']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['business']}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}

        {visibleTab === 'reference' && (
          <Filters
            className={'referenceDataflowsListFilters'}
            data={dataToFilter['reference']}
            getFilteredData={onLoadFilteredData}
            options={filterOptions['reference']}
            sortCategory={'pinned'}
            sortable={true}
          />
        )}
      </div>

      {renderContent()}
    </div>
  );
};

export { DataflowsList };
