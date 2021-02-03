import React, { useContext, useEffect, useState } from 'react';

import intersection from 'lodash/intersection';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import orderBy from 'lodash/orderBy';
import pull from 'lodash/pull';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'ui/views/_components/Filters';

import { UserService } from 'core/services/User';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { DataflowsListUtils } from './_functions/Utils/DataflowsListUtils';

const DataflowsList = ({ className, content = [], dataFetch, description, isCustodian, title, type }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataToFilter, setDataToFilter] = useState(content);
  const [filteredData, setFilteredData] = useState(dataToFilter);
  const [filteredState, setFilteredState] = useState(false);
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

  const onLoadFiltredData = data => setFilteredData(data);

  const changeUserProperties = async userProperties => {
    try {
      const response = await UserService.updateAttributes(userProperties);
      return response;
    } catch (error) {
      notificationContext.add({
        type: 'UPDATE_ATTRIBUTES_USER_SERVICE_ERROR'
      });
    }
  };

  const getFilteredSearched = value => setFilteredState(value);

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
      const changedInitialdData = inmDataToFilter.map(item => {
        if (item.id === pinnedItem.id) {
          item.pinned = isPinned ? 'pinned' : 'unpinned';
        }
        return item;
      });

      setDataToFilter(
        orderBy(changedInitialdData, ['pinned', 'expirationDate', 'status', 'id'], ['asc', 'asc', 'asc', 'asc'])
      );
    }
  };

  return (
    <div className={`${styles.wrap} ${className}`}>
      {title && <h2>{title}</h2>}
      <p>{description}</p>
      <div className="dataflowList-filters-help-step">
        <Filters
          data={dataToFilter}
          dateOptions={DataflowConf.filterItems['date']}
          getFilteredData={onLoadFiltredData}
          getFilteredSearched={getFilteredSearched}
          inputOptions={DataflowConf.filterItems['input']}
          selectOptions={DataflowConf.filterItems['select']}
          sortable={true}
          sortCategory={'pinned'}
        />
      </div>

      {!isEmpty(content) ? (
        !isEmpty(filteredData) ? (
          filteredData.map((dataflow, i) => (
            <>
              <DataflowsItem
                dataFetch={dataFetch}
                isCustodian={isCustodian}
                itemContent={dataflow}
                key={dataflow.id}
                reorderDataflows={reorderDataflows}
                type={type}
              />
              {!isFilteredByPinned() && pinnedSeparatorIndex === i ? <hr className={styles.pinnedSeparator} /> : null}
            </>
          ))
        ) : (
          <div className={styles.noDataflows}>{resources.messages['noDataflowsWithSelectedParameters']}</div>
        )
      ) : (
        <div className={styles.noDataflows}>{resources.messages['thereAreNoDatalows']}</div>
      )}
    </div>
  );
};

export { DataflowsList };
