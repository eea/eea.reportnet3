import React, { useContext, useEffect } from 'react';

import styles from './SQLsentence.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SQLsentence = ({ creationFormState, onSetSQLsentence }) => {
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    return () => onSetSQLsentence('sqlSentence', '');
  }, []);

  return (
    <React.Fragment>
      <div className={styles.section}>
        <h3>SQL sentence:</h3>

        <textarea
          cols={30}
          id="SQLsentenceTextarea"
          name=""
          onChange={e => onSetSQLsentence('sqlSentence', e.target.value)}
          rows={10}
          value={creationFormState.candidateRule['sqlSentence']}
        />
      </div>
    </React.Fragment>
  );
};
