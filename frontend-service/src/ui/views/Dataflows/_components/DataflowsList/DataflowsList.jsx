import React, { useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './DataflowsList.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { DataflowsItem } from './_components/DataflowsItem';
import { Filters } from './_components/Filters';

const DataflowsList = ({ className, content, dataFetch, description, title, type }) => {
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
      {!isNil(filteredData) &&
        filteredData.map(dataflow => (
          <DataflowsItem dataFetch={dataFetch} itemContent={dataflow} key={dataflow.id} type={type} />
        ))}
    </div>
  );
};

export { DataflowsList };
