import React, { useContext, useRef } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import style from './BigButton.module.css';

import { Menu } from './_components/Menu';
import { Icon } from 'ui/views/_components/Icon';

export const BigButton = ({ layout, handleRedirect, model, caption, isReleased }) => {
  let menuRef = useRef();
  const dataset = model ? (
    <>
      <div className={`${style.bigButton} ${style.dataset}`}>
        <a
          href=""
          onClick={e => {
            e.preventDefault();
            handleRedirect();
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('dataset')} />
        </a>
        <span className={style.dropDwonIcon} onClick={e => menuRef.current.show(e)}>
          <FontAwesomeIcon icon={AwesomeIcons('dropDown')} />
        </span>
        <Menu ref={menuRef} model={model} />
        {isReleased && (
          <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />
        )}
      </div>
      <p className={style.caption}>{caption}</p>
    </>
  ) : (
    <></>
  );
  const documents = (
    <>
      <div className={`${style.bigButton} ${style.documents}`}>
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
      <div className={`${style.bigButton} ${style.dashboard}`}>
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
    dataset,
    documents,
    dashboard
  };
  return <div className={`${style.datasetItem}`}>{buttons[layout]}</div>;
};
