import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from 'ui/views/_components/Filters';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowsListUtils } from './_functions/Utils/DataflowsListUtils';

const DataflowsList = ({ className, content = [], dataFetch, description, title, type }) => {
  const resources = useContext(ResourcesContext);

  const [dataToFilter, setDataToFilter] = useState(content);
  const [filteredData, setFilteredData] = useState(dataToFilter);

  useEffect(() => {
    setDataToFilter(DataflowsListUtils.parseDataToFilter(content));
  }, [content]);

  const onLoadFiltredData = data => setFilteredData(data);

  return (
    <div className={`${styles.wrap} ${className}`}>
      <h2>{title}</h2>
      <p>{description}</p>
      <Filters
        data={dataToFilter}
        dateOptions={DataflowConf.filterItems['date']}
        getFilteredData={onLoadFiltredData}
        inputOptions={DataflowConf.filterItems['input']}
        selectOptions={DataflowConf.filterItems['select']}
        sortable={true}
      />
      {!isEmpty(content) ? (
        !isEmpty(filteredData) ? (
          filteredData.map(dataflow => (
            <DataflowsItem dataFetch={dataFetch} itemContent={dataflow} key={dataflow.id} type={type} />
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
