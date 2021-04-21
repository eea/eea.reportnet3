import { useEffect, useReducer } from 'react';

import styles from './TabularSwitch.module.scss';

import { tabularSwitchReducer } from './_functions/Reducers/tabularSwitchReducer';

import { TabularSwitchUtils } from './_functions/Utils/TabularSwitchUtils';

const TabularSwitch = ({
  className = '',
  elements = [],
  id,
  getIsTableCreated = () => {},
  isTableCreated,
  onChange,
  value = ''
}) => {
  const { onSwitchAnimate, parseViews } = TabularSwitchUtils;

  const [tabularSwitchState, tabularSwitchDispatch] = useReducer(tabularSwitchReducer, {
    views: parseViews(elements, value)
  });

  const { views } = tabularSwitchState;

  useEffect(() => {
    if (isTableCreated) {
      onSwitchView(value);
    }
  }, [isTableCreated]);

  const onSwitchView = element => {
    const viewType = { ...views };
    Object.keys(viewType).forEach(view => {
      viewType[view] = false;
      viewType[element] = true;
    });

    onChange(element);
    tabularSwitchDispatch({ type: 'ON_CHANGE_VIEW', payload: { viewType } });
    getIsTableCreated(false);
  };

  return (
    <div className={`${className} ${styles.tabBar}`} id={id}>
      <div className={styles.indicator} style={{ left: `calc(${onSwitchAnimate(views) * 150}px + 1.5rem)` }} />
      {elements.map((element, i) => (
        <div
          className={`${styles.tabItem} ${views[element] ? styles.selected : null}`}
          key={i}
          onClick={() => onSwitchView(element)}>
          <p className={styles.tabLabel}>{element}</p>
        </div>
      ))}
    </div>
  );
};

export { TabularSwitch };
