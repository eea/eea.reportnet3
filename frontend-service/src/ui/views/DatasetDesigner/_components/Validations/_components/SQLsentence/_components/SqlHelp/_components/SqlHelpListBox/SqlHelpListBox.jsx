import { useContext } from 'react';

import styles from './SqlHelpListBox.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SqlHelpListBox = ({
  level,
  onAddHelpItem,
  onChange,
  options,
  selectedItem,
  title,
  isSpinnerVisible = false
}) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className={styles.section}>
      <div className={styles.title}>
        <h3>{title} </h3>
        <Button
          className={`${styles.addButton} rp-btn secondary`}
          type="submit"
          icon="angleDoubleRight"
          onClick={e => {
            e.preventDefault();
            onAddHelpItem(level);
          }}
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
        spinner={isSpinnerVisible}
      />
    </div>
  );
};
