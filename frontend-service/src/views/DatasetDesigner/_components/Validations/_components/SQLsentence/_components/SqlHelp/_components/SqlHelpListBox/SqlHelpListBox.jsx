import styles from './SqlHelpListBox.module.scss';

import { Button } from 'views/_components/Button';
import { ListBox } from 'views/DatasetDesigner/_components/ListBox';

export const SqlHelpListBox = ({
  level,
  onAddHelpItem,
  onChange,
  options,
  selectedItem,
  title,
  isSpinnerVisible = false
}) => {
  return (
    <div className={styles.section}>
      <div className={styles.title}>
        <h3>{title} </h3>
        <Button
          className={`${styles.addButton} rp-btn secondary`}
          icon="angleDoubleRight"
          onClick={e => {
            e.preventDefault();
            onAddHelpItem(level);
          }}
          type="submit"
        />
      </div>
      <ListBox
        listStyle={{ height: '100px' }}
        onChange={e => onChange(e.value)}
        optionLabel="label"
        optionValue="value"
        options={options}
        spinner={isSpinnerVisible}
        value={selectedItem}
      />
    </div>
  );
};
