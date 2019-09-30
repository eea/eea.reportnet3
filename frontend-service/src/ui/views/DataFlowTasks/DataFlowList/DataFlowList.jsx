import React from 'react';

import styles from './DataFlowList.module.scss';

import { DataflowItem } from './DataFlowItem';

export const DataflowList = ({ listTitle, listDescription, listContent, listType, dataFetch }) => {
  return (
    <div className={styles.wrap}>
      <h2>{listTitle}</h2>
      <p>{listDescription}</p>

      {listContent.map(item => {
        return <DataflowItem key={item.id} itemContent={item} listType={listType} dataFetch={dataFetch} />;
      })}
    </div>
  );
};
