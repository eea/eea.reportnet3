import React, { useContext, useEffect } from 'react';

import styles from './SQLsentence.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SQLsentence = ({ creationFormState, onSetSQLsentence, level }) => {
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    return () => onSetSQLsentence('sqlSentence', '');
  }, []);

  const levelTypes = {
    FIELD: 'field',
    ROW: 'row',
    TABLE: 'dataset'
  };

  const getHelpByLevel = level => {
    if (level === levelTypes.FIELD) {
      return resources.messages['sqlSentenceHelpField'];
    }

    if (level === levelTypes.ROW) {
      return resources.messages['sqlSentenceHelpRow'];
    }

    return resources.messages['sqlSentenceHelpTable'];
  };

  return (
    <div className={styles.section}>
      <h3 className={styles.title}>{resources.messages['sqlSentence']}:</h3>
      <p>{resources.messages['sqlSentenceHelpDescription']}</p>
      <p className={styles.levelHelp} dangerouslySetInnerHTML={{ __html: getHelpByLevel(level) }}></p>
      <p className={styles.note} dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceHelpNote'] }}></p>
      <textarea
        cols={30}
        id="SQLsentenceTextarea"
        name=""
        onChange={e => onSetSQLsentence('sqlSentence', e.target.value)}
        rows={10}
        value={creationFormState.candidateRule['sqlSentence']}
      />
    </div>
  );
};
