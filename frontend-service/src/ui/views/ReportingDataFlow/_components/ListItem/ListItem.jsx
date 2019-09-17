import React from 'react';

import style from './ListItem.module.css';
export const ListItem = ({ layout }) => {
  const dataSet = <div className={`${style.listItem} ${style.dataSet}`}></div>;
  const documents = <div></div>;
  const buttons = {
    dataSet,
    documents
  };
  return buttons[layout];
};
