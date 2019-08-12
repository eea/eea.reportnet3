import React from 'react';

import styles from './DataFlowList.module.scss';

import { DataFlowItem } from './DataFlowItem';

export const DataFlowList = ({ listTitle, listDescription, listContent, listType, dataFetch }) => {
  return (
    <div className={styles.wrap}>
      <h2>{listTitle}</h2>
      <p>{listDescription}</p>

      {listContent.map(item => {
        return <DataFlowItem key={item.id} itemContent={item} listType={listType} dataFetch={dataFetch} />;
      })}
    </div>
  );
};
