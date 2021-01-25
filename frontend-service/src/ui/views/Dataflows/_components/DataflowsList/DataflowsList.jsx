import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
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
    setDataToFilter(
      orderBy(
        DataflowsListUtils.parseDataToFilter(content, userContext.userProps.pinnedDataflows),
        ['pinned', 'expirationDate', 'status'],
        ['desc', 'asc', 'asc']
      )
    );
  }, [content]);

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
    filteredData.filter(dataflow => dataflow.pinned === true).length === filteredData.length ||
    filteredData.filter(dataflow => dataflow.pinned === false).length === filteredData.length;

  const reorderDataflows = async (pinnedItem, isPinned) => {
    const inmUserProperties = { ...userContext.userProps };
    const inmPinnedDataflows = inmUserProperties.pinnedDataflows;
    console.log({ inmPinnedDataflows });
    if (!isEmpty(inmPinnedDataflows) && inmPinnedDataflows.includes(pinnedItem.id.toString())) {
      pull(inmPinnedDataflows, pinnedItem.id.toString());
    } else {
      inmPinnedDataflows.push(pinnedItem.id.toString());
    }
    inmUserProperties.pinnedDataflows = inmPinnedDataflows;
    const response = await changeUserProperties(inmUserProperties);
    if (response.status >= 200 && response.status <= 299) {
      userContext.onChangePinnedDataflows(inmPinnedDataflows);
    }

    const inmfilteredData = [...filteredData];
    const changedFilteredData = inmfilteredData.map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned;
      }
      return item;
    });
    console.log('LLEGO');
    const orderedFilteredData = orderBy(
      changedFilteredData,
      ['pinned', 'expirationDate', 'status'],
      ['desc', 'asc', 'asc']
    );

    const orderedPinned = orderedFilteredData.map(el => el.pinned);
    setPinnedSeparatorIndex(orderedPinned.lastIndexOf(true));
    setFilteredData(orderedFilteredData);
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
