import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import style from './ListItem.module.css';

import { Menu } from './_components/Menu';
import { Icon } from 'ui/views/_components/Icon';

export const ListItem = ({ layout, label, handleRedirect, model }) => {
  const toggleVisibility = target => {
    const incommingMenu = target.nextSibling;
    const display = incommingMenu.style.display === 'none' ? true : false;

    const allMenus = document.querySelectorAll('.p-menu-overlay-visible');
    allMenus.forEach(other => (other.style.display = 'none'));
    if (display) {
      incommingMenu.style.display = 'block';
      setTimeout(() => {
        incommingMenu.style.bottom = `-${incommingMenu.offsetHeight}px`;
        incommingMenu.style.opacity = 1;
      }, 50);
    } else {
      incommingMenu.style.display = 'none';
    }
  };

  const dataSet = model ? (
    <div className={`${style.listItem} ${style.dataSet}`}>
      <a
        href=""
        onClick={e => {
          e.preventDefault();
          handleRedirect();
        }}>
        <FontAwesomeIcon icon={AwesomeIcons('dataSet')} />
      </a>
      <span className={style.dropDwonIcon} onClick={e => toggleVisibility(e.currentTarget)}>
        <FontAwesomeIcon icon={AwesomeIcons('dropDown')} />
      </span>
      <Menu model={model} />
      {true && <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />}
    </div>
  ) : (
    <></>
  );
  const documents = (
    <div className={`${style.listItem} ${style.documents}`}>
      <a
        href=""
        onClick={e => {
          e.preventDefault();
          handleRedirect();
        }}>
        <FontAwesomeIcon icon={AwesomeIcons('file')} />
      </a>
    </div>
  );
  const dashboard = (
    <div className={`${style.listItem} ${style.dashboard}`}>
      <a
        href=""
        onClick={e => {
          e.preventDefault();
          handleRedirect();
        }}>
        <FontAwesomeIcon icon={AwesomeIcons('barChart')} />
      </a>
    </div>
  );
  const buttons = {
    dataSet,
    documents,
    dashboard
  };
  return buttons[layout];
};
