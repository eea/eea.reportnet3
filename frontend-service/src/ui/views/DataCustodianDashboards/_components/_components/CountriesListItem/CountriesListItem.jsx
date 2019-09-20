import React from 'react';

function CountriesListItem({ filterDispatch, item }) {
  return (
    <li>
      <input
        id={item.tableId}
        type="checkbox"
        defaultChecked={true}
        onChange={e => {
          if (e.target.checked) {
            filterDispatch({
              type: 'COUNTRY_CHECKBOX_ON',
              payload: { label: item }
            });
          } else {
            filterDispatch({
              type: 'COUNTRY_CHECKBOX_OFF',
              payload: { label: item }
            });
          }
        }}
      />
      <label htmlFor={item}>{item}</label>
    </li>
  );
}

export { CountriesListItem };
