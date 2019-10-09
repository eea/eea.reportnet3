import React, { useContext, useRef } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import style from './BigButton.module.css';

import { DropdownButton } from 'ui/views/_components/DropdownButton';
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
        <DropdownButton
          icon="caretDown"
          model={model}
          buttonStyle={{ position: 'absolute', bottom: '-5px', right: '0px' }}
        />
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
