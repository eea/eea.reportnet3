import React from 'react';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './DataflowsItem';

const DataflowsList = ({ dataFetch, dataflowNewValues, content, description, title, type, selectedDataflowId }) => {
  return (
    <div className={styles.wrap}>
      <h2>{title}</h2>
      <p>{description}</p>

      {content.map(item => {
        return (
          <DataflowsItem
            key={item.id}
            dataFetch={dataFetch}
            dataflowNewValues={dataflowNewValues}
            itemContent={item}
            type={type}
            selectedDataflowId={selectedDataflowId}
          />
        );
      })}
    </div>
  );
};

export { DataflowsList };
