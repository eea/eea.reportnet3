import { useContext } from 'react';

import styles from './UnlockReferenceDataflow.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const UnlockReferenceDataflow = ({ updatable, toggleUpdatable }) => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.wrapper}>
      <Checkbox checked={updatable} onChange={() => toggleUpdatable()} role="checkbox" />
      <label htmlFor="">{resourcesContext.messages['unlockReferenceDataflowLabel']}</label>
    </div>
  );
};
