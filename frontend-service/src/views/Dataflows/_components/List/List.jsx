import React from 'react';
import { DataflowsItem } from '../DataflowsList/_components/DataflowsItem';

export const List = ({ dataflows }) => {
  console.log('dataflows :>> ', dataflows);

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
