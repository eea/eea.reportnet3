import React, { useState } from 'react';

function TableListItem({ filterDispatch, item }) {
  return (
    <li>
      <input
        id={item.tableId}
        type="checkbox"
        defaultChecked={true}
        onChange={e => {
          if (e.target.checked) {
            filterDispatch({
              type: 'TABLE_CHECKBOX_ON',
              payload: { tableId: item.tableId }
            });
          } else {
            filterDispatch({
              type: 'TABLE_CHECKBOX_OFF',
              payload: { tableId: item.tableId }
            });
          }
        }}
      />
      <label htmlFor={item.tableId}>{item.tableName}</label>
    </li>
  );
}

export { TableListItem };
