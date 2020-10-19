import React, { useContext, useEffect } from 'react';

import styles from './SQLsentence.module.scss';

import { SqlHelp } from './_components/SqlHelp';

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
    } else if (level === levelTypes.ROW) {
      return resources.messages['sqlSentenceHelpRow'];
    } else {
      return resources.messages['sqlSentenceHelpTable'];
    }
  };

  return (
    <div className={styles.section}>
      <div className={styles.content}>
        <div className={styles.helpSideBar}>
          <SqlHelp onSetSqlSentence={onSetSQLsentence} sqlSentence={creationFormState.candidateRule['sqlSentence']} />
        </div>
        <div className={styles.sqlSentence}>
          <h3 className={styles.title}>{resources.messages['sqlSentence']}:</h3>
          <p>{resources.messages['sqlSentenceHelpDescription']}</p>
          <p className={styles.levelHelp} dangerouslySetInnerHTML={{ __html: getHelpByLevel(level) }}></p>
          <p
            className={styles.note}
            dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceHelpNote'] }}></p>
          <textarea
            id="SQLsentenceTextarea"
            name=""
            onChange={e => onSetSQLsentence('sqlSentence', e.target.value)}
            value={creationFormState.candidateRule['sqlSentence']}
          />
        </div>
      </div>
    </div>
  );
};
