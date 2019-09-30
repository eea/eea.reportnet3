import React, { useContext } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import style from './BigButton.module.css';

import { Menu } from './_components/Menu';
import { Icon } from 'ui/views/_components/Icon';

import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const BigButton = ({ layout, handleRedirect, model, caption }) => {
  const resources = useContext(ResourcesContext);
  const toggleVisibility = target => {
    const incommingMenu = target.nextSibling;
    const display = incommingMenu.style.display === 'none' ? true : false;

    const allMenus = document.querySelectorAll('.p-menu-overlay-visible');
    allMenus.forEach(other => (other.style.display = 'none'));
    if (display) {
      incommingMenu.style.opacity = 0;
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
    <>
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
      <p className={style.caption}>{caption}</p>
    </>
  ) : (
    <></>
  );
  const documents = (
    <>
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
      <p className={style.caption}>{caption}</p>
    </>
  );
  const dashboard = (
    <>
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
      <p className={style.caption}>{caption}</p>
    </>
  );
  const buttons = {
    dataSet,
    documents,
    dashboard
  };
  return <div className={`${style.dataSetItem}`}>{buttons[layout]}</div>;
};
