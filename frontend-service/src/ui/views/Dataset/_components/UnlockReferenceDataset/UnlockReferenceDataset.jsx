import { useContext } from 'react';

import styles from './UnlockReferenceDataset.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const UnlockReferenceDataset = ({ updatable, toggleUpdatable }) => {
  const resourcesContext = useContext(ResourcesContext);

  return (
    <div className={styles.wrapper}>
      <Checkbox
        checked={updatable}
        id="referenceDatasetUpdatableCheckbox"
        onChange={() => toggleUpdatable()}
        role="checkbox"
      />
      <label htmlFor="referenceDatasetUpdatableCheckbox">
        {resourcesContext.messages['unlockReferenceDatasetLabel']}
      </label>
    </div>
  );
};
