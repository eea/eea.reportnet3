import React, { useContext } from 'react';

import styles from './SqlHelpListBox.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SqlHelpListBox = ({ onChange, options, selectedItem, title }) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className={styles.section}>
      <div className={styles.title}>
        <h3>{title} </h3>
        <Button
          className={`${styles.addButton} rp-btn secondary`}
          tooltip={resourcesContext.messages.createSnapshotTooltip}
          type="submit"
          icon="plus"
        />
      </div>
      <ListBox
        listStyle={{ height: '100px' }}
        onChange={e => {}}
        optionLabel="label"
        optionValue="value"
        options={options}
        value={selectedItem}
        onChange={e => onChange(e.value)}
      />
    </div>
  );
};
