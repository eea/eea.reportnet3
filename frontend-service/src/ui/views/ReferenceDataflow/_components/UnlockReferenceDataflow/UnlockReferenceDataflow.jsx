import { useContext } from 'react';

import styles from './UnlockReferenceDataflow.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const UnlockReferenceDataflow = ({ updateable, toggleUnlockedLocked }) => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.wrapper}>
      <Checkbox checked={updateable} onClick={() => toggleUnlockedLocked()} role="checkbox" />
      <label htmlFor="">{resourcesContext.messages['unlockReferenceDataflowLabel']}</label>
    </div>
  );
};
