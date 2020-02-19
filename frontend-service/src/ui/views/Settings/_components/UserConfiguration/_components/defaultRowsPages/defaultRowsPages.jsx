import React, { Component, useState, useContext } from 'react';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { DropdownRowsPages } from './_components/DropdownRowsPages';

export const DefaultRowsPages = () => {
  const resources = useContext(ResourcesContext);
  const arr = [1, 2, 3];
  const [rowSelected, setRowSelected] = useState({ row: null });
  return (
    <React.Fragment>
      {' '}
      <h3>{resources.messages['defaultRowsPage']}</h3>
      <DropdownRowsPages
        name="rows per page"
        options={arr}
        optionLabel="label"
        placeholder={'select rows per page madafaca'}
        value={rowSelected}
        onChange={e => {
          setRowSelected({ row: e.value });
        }}
      />
    </React.Fragment>
  );
};
