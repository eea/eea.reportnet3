import React from 'react';

import style from './ListItem.module.css';
export const ListItem = ({ layout, label, handleRedirect }) => {
  const dataSet = <div className={`${style.listItem} ${style.dataSet}`}>{label}</div>;
  const documents = (
    <div className={`${style.listItem} ${style.documents}`}>
      <a href="" onClick={() => handleRedirect()}>
        {label}
      </a>
    </div>
  );
  const buttons = {
    dataSet,
    documents
  };
  return buttons[layout];
};
