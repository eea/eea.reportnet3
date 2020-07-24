import React from 'react';
import { Toolbar as PrimeToolbar } from 'primereact/toolbar';

export const Toolbar = React.memo(({ className, id, style, children }) => {
  return (
    <PrimeToolbar className={`${className} datasetSchema-toolbar-dataset-data-help-step`} id={id} style={style}>
      {children}
    </PrimeToolbar>
  );
});
