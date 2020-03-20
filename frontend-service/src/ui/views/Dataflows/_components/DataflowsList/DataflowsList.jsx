import React, { useContext, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from './_components/Filters';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const DataflowsList = ({ className, content = [], dataFetch, description, title, type }) => {
  const resources = useContext(ResourcesContext);

  const [filteredData, setFilteredData] = useState(content);

  const onLoadFiltredData = data => {
    setFilteredData(data);
  };

  return (
    <div className={`${styles.wrap} ${className}`}>
      <h2>{title}</h2>
      <p>{description}</p>
      <Filters
        data={content}
        dateOptions={DataflowConf.filterItems['date']}
        getFiltredData={onLoadFiltredData}
        inputOptions={DataflowConf.filterItems['input']}
        selectOptions={DataflowConf.filterItems['select']}
      />
      {!isNil(filteredData) && !isEmpty(filteredData) ? (
        filteredData.map(dataflow => (
          <DataflowsItem dataFetch={dataFetch} itemContent={dataflow} key={dataflow.id} type={type} />
        ))
      ) : (
        <div className={styles.noDataflows}>{resources.messages['noDataflowsWithSelectedParameters']}</div>
      )}
      {isEmpty(content) && <div className={styles.noDataflows}>{resources.messages['thereAreNoDatalows']}</div>}
    </div>
  );
};

export { DataflowsList };
