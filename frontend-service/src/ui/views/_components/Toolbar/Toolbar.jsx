import React from 'react';
import { Toolbar as PrimeToolbar } from 'primereact/toolbar';

export const Toolbar = ({ className, id, style, children }) => {
  return (
    <PrimeToolbar className={className} id={id} style={style}>
      {children}
    </PrimeToolbar>
  );
};
