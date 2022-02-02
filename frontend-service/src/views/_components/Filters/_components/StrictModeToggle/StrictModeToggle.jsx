import { useContext } from 'react';
import { useRecoilState } from 'recoil';

import styles from '../../Filters.module.scss';

import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';

import { isStrictModeStore } from 'views/_components/Filters/_functions/Stores/filterStore';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const StrictModeToggle = ({ onFilter, onToggle }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isStrictMode, setIStrictMode] = useRecoilState(isStrictModeStore);

  const onToggleStrictMode = async () => {
    setIStrictMode(prevState => !prevState);

    onToggle({ type: 'STRICT_MODE' });
    await onFilter({ type: 'STRICT_MODE', isStrictMode: !isStrictMode });
  };

  return (
    <span className={styles.checkboxWrap} data-for="checkboxTooltip" data-tip>
      {resourcesContext.messages['strictModeCheckboxFilter']}
      <Button
        className={`${styles.strictModeInfoButton} p-button-rounded p-button-secondary-transparent`}
        icon="infoCircle"
        tooltip={resourcesContext.messages['strictModeTooltip']}
        tooltipOptions={{ position: 'top' }}
      />
      <span className={styles.checkbox}>
        <Checkbox
          ariaLabel={resourcesContext.messages['strictModeCheckboxFilter']}
          checked={isStrictMode}
          id="matchMode_checkbox"
          inputId="matchMode_checkbox"
          onChange={onToggleStrictMode}
          role="checkbox"
        />
      </span>
    </span>
  );
};
