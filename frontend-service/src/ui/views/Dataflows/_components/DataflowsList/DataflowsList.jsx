import { Fragment, useContext, useEffect, useState } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';
import pull from 'lodash/pull';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'ui/views/_components/Filters';

import { UserService } from 'core/services/User';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { DataflowsListUtils } from './_functions/Utils/DataflowsListUtils';

const DataflowsList = ({ className, content = [], description, isCustodian, title }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataToFilter, setDataToFilter] = useState(content);
  const [filteredData, setFilteredData] = useState(dataToFilter);
  const [pinnedSeparatorIndex, setPinnedSeparatorIndex] = useState(-1);

  useEffect(() => {
    const parsedDataflows = orderBy(
      DataflowsListUtils.parseDataToFilter(content, userContext.userProps.pinnedDataflows),
      ['pinned', 'expirationDate', 'status', 'id'],
      ['asc', 'asc', 'asc', 'asc']
    );
    setDataToFilter(parsedDataflows);
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
      return await UserService.updateAttributes(userProperties);
    } catch (error) {
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
      dataToFilter.map(data => data.id.toString())
    );
    if (!isEmpty(inmPinnedDataflows) && inmPinnedDataflows.includes(pinnedItem.id.toString())) {
      pull(inmPinnedDataflows, pinnedItem.id.toString());
    } else {
      inmPinnedDataflows.push(pinnedItem.id.toString());
    }
    inmUserProperties.pinnedDataflows = inmPinnedDataflows;

    const response = await changeUserProperties(inmUserProperties);
    if (!isNil(response) && response.status >= 200 && response.status <= 299) {
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

      const inmDataToFilter = [...dataToFilter];
      const changedInitialData = inmDataToFilter.map(item => {
        if (item.id === pinnedItem.id) {
          item.pinned = isPinned ? 'pinned' : 'unpinned';
        }
        return item;
      });

      setDataToFilter(
        orderBy(changedInitialData, ['pinned', 'expirationDate', 'status', 'id'], ['asc', 'asc', 'asc', 'asc'])
      );
    }
  };

  const filterOptions = [
    {
      type: 'input',
      properties: [{ name: 'name' }, { name: 'description' }, { name: 'legalInstrument' }, { name: 'obligationTitle' }]
    },
    { type: 'multiselect', properties: [{ name: 'status' }, { name: 'userRole' }, { name: 'pinned' }] },
    { type: 'date', properties: [{ name: 'expirationDate' }] }
  ];

  return (
    <div className={`${styles.wrap} ${className}`}>
      {title && <h2>{title}</h2>}
      <p>{description}</p>
      <div className="dataflowList-filters-help-step">
        <Filters
          data={dataToFilter}
          getFilteredData={onLoadFilteredData}
          options={filterOptions}
          sortCategory={'pinned'}
          sortable={true}
        />
      </div>

      {!isEmpty(content) ? (
        !isEmpty(filteredData) ? (
          filteredData.map((dataflow, i) => (
            <Fragment key={dataflow.id}>
              <DataflowsItem isCustodian={isCustodian} itemContent={dataflow} reorderDataflows={reorderDataflows} />
              {!isFilteredByPinned() && pinnedSeparatorIndex === i ? <hr className={styles.pinnedSeparator} /> : null}
            </Fragment>
          ))
        ) : (
          <div className={styles.noDataflows}>{resources.messages['noDataflowsWithSelectedParameters']}</div>
        )
      ) : (
        <div className={styles.noDataflows}>{resources.messages['thereAreNoDataflows']}</div>
      )}
    </div>
  );
};

export { DataflowsList };
