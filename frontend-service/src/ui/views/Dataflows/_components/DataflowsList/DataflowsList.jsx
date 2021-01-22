import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import orderBy from 'lodash/orderBy';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'ui/views/_components/Filters';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowsListUtils } from './_functions/Utils/DataflowsListUtils';

const DataflowsList = ({ className, content = [], dataFetch, description, isCustodian, title, type }) => {
  const resources = useContext(ResourcesContext);

  const [dataToFilter, setDataToFilter] = useState(content);
  const [filteredData, setFilteredData] = useState(dataToFilter);
  const [filteredState, setFilteredState] = useState(false);
  console.log({ content });
  useEffect(() => {
    setDataToFilter(
      orderBy(
        DataflowsListUtils.parseDataToFilter(content),
        ['pinned', 'expirationDate', 'status'],
        ['desc', 'asc', 'asc']
      )
    );
  }, [content]);

  const onLoadFiltredData = data => setFilteredData(data);

  const getFilteredSearched = value => setFilteredState(value);

  const reorderDataflows = (pinnedItem, isPinned) => {
    const inmfilteredData = [...filteredData];
    const changedFilteredData = inmfilteredData.map(item => {
      if (item.id === pinnedItem.id) {
        item.pinned = isPinned;
      }
      return item;
    });
    console.log(orderBy(changedFilteredData, ['pinned', 'expirationDate', 'status'], ['desc', 'asc', 'asc']));
    setFilteredData(orderBy(changedFilteredData, ['pinned', 'expirationDate', 'status'], ['desc', 'asc', 'asc']));
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
      {console.log(filteredData)}
      {!isEmpty(content) ? (
        !isEmpty(filteredData) ? (
          filteredData.map(dataflow => (
            <DataflowsItem
              dataFetch={dataFetch}
              isCustodian={isCustodian}
              itemContent={dataflow}
              key={dataflow.id}
              reorderDataflows={reorderDataflows}
              type={type}
            />
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
