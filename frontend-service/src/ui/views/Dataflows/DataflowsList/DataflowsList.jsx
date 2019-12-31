import React from 'react';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './DataflowsItem';

const DataflowsList = ({ dataFetch, dataflowNewValues, content, description, title, type, selectedDataflowId }) => {
  //position property and counter are only for presentation purpouse and must be removed in def implementation
  let counter = 0;
  return (
    <div className={styles.wrap}>
      <h2>{title}</h2>
      <p>{description}</p>

      {content.map(item => {
        counter += 1;
        return (
          <DataflowsItem
            key={item.id}
            dataFetch={dataFetch}
            dataflowNewValues={dataflowNewValues}
            itemContent={item}
            type={type}
            position={counter}
            selectedDataflowId={selectedDataflowId}
          />
        );
      })}
    </div>
  );
};

export { DataflowsList };
