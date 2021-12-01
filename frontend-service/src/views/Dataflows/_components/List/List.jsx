import { isEmpty } from 'lodash';
import React from 'react';
import { DataflowsItem } from '../DataflowsList/_components/DataflowsItem';

export const List = ({ dataflows }) => {
  if (isEmpty(dataflows)) {
    return <div style={{ margin: '5rem' }}>LOADING</div>;
  }

  return (
    <div>
      {dataflows.map(dataflow => (
        <DataflowsItem
          isAdmin={false}
          isCustodian={true}
          itemContent={dataflow}
          key={dataflow.id}
          // reorderDataflows={reorderDataflows}
        />
      ))}
    </div>
  );
};
