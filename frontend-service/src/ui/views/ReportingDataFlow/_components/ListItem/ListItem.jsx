import React, { useCallback, useEffect, useReducer } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import style from './ListItem.module.css';

import { Menu } from './_components/Menu';
import { Icon } from 'ui/views/_components/Icon';

export const ListItem = ({ layout, label, handleRedirect, model }) => {
  const reducer = (state, { type, payload }) => {
    switch (type) {
      case 'TOGGLE_MENU':
        return {
          ...state,
          hidden: !state.hidden,
          menu: payload ? payload.target.nextSibling : null
        };
      case 'POSITIONING_MENU':
        return {
          ...state
        };

      default:
        return state;
    }
  };
  const dropdownInitialState = {
    hidden: true,
    menu: null
  };

  const [dropdownState, dropdowndispatch] = useReducer(reducer, dropdownInitialState);
  useEffect(() => {
    if (!dropdownState.hidden) {
      dropdownState.menu.style.bottom = `-${dropdownState.menu.offsetHeight}px`;
      dropdownState.menu.style.opacity = 1;
    }
  }, [dropdownState]);
  const dataset = model ? (
    <div className={`${style.listItem} ${style.dataset}`}>
      <a
        href=""
        onClick={e => {
          e.preventDefault();
          handleRedirect();
        }}>
        <FontAwesomeIcon icon={AwesomeIcons('dataset')} />
      </a>
      <span
        className={style.dropDwonIcon}
        onClick={e => dropdowndispatch({ type: 'TOGGLE_MENU', payload: { target: e.currentTarget } })}>
        <FontAwesomeIcon icon={AwesomeIcons('dropDown')} />
      </span>
      <Menu dropdownState={dropdownState} dropdowndispatch={dropdowndispatch} model={model} />
      {false && <Icon style={{ position: 'absolute', top: '0', right: '0', fontSize: '1.8rem' }} icon="cloudUpload" />}
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
    dataset,
    documents,
    dashboard
  };
  return buttons[layout];
};
