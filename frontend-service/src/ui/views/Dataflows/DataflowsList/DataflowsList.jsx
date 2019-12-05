import React from 'react';

import styles from './DataflowsList.module.scss';

import { DataflowsItem } from './DataflowsItem';

export const DataflowsList = ({ listTitle, listDescription, listContent, listType, dataFetch }) => {
  //position property and counter are only for presentation purpouse and must be removed in def implementation
  let counter = 0;
  return (
    <div className={styles.wrap}>
      <h2>{listTitle}</h2>
      <p>{listDescription}</p>

      {listContent.map(item => {
        counter += 1;
        return (
          <DataflowsItem key={item.id} itemContent={item} listType={listType} dataFetch={dataFetch} position={counter} />
        );
      })}
    </div>
  );
};
