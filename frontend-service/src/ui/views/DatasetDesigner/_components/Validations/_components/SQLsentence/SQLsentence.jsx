import { useContext, useEffect, useState } from 'react';

import styles from './SQLsentence.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { SqlHelp } from './_components/SqlHelp';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SQLsentence = ({ creationFormState, onSetSQLsentence, level }) => {
  const resources = useContext(ResourcesContext);

  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);

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

  const onClickInfoButton = () => {
    setIsVisibleInfoDialog(true);
  };

  const onHideInfoDiaog = () => {
    setIsVisibleInfoDialog(false);
  };

  const onCCButtonClick = () => {
    onSetSQLsentence('sqlSentence', `${creationFormState.candidateRule['sqlSentence']} ${countryCodeKeyword}`);
  };

  const countryCodeKeyword = `${config.COUNTRY_CODE_KEYWORD}`;

  return (
    <div className={styles.section}>
      <div className={styles.content}>
        <div className={styles.helpSideBar}>
          <SqlHelp onSetSqlSentence={onSetSQLsentence} sqlSentence={creationFormState.candidateRule['sqlSentence']} />
        </div>
        <div className={styles.sqlSentence}>
          <h3 className={styles.title}>
            {resources.messages['sqlSentence']}
            <Button
              className={`${styles.sqlSentenceInfoBtn} p-button-rounded p-button-secondary-transparent`}
              icon="infoCircle"
              id="infoSqlSentence"
              onClick={e => onClickInfoButton()}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label={resources.messages['countryCodeAcronym']}
              onClick={onCCButtonClick}
              tooltip={resources.messages['matchStringTooltip']}
              tooltipOptions={{ position: 'top' }}
            />
          </h3>
          <textarea
            id="sqlSentenceText"
            name=""
            onChange={e => onSetSQLsentence('sqlSentence', e.target.value)}
            value={creationFormState.candidateRule['sqlSentence']}></textarea>
        </div>
      </div>
      {isVisibleInfoDialog && (
        <Dialog
          header={resources.messages['sqlSentenceHelpDialogTitle']}
          onHide={onHideInfoDiaog}
          style={{ maxWidth: '41vw' }}
          visible={isVisibleInfoDialog}>
          <p className={styles.levelHelp} dangerouslySetInnerHTML={{ __html: getHelpByLevel(level) }}></p>
          <p
            className={styles.note}
            dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceHelpNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceSpatialNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceSpatialTypesNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resources.messages['sqlSentenceCountryCodeNote'] }}></p>
        </Dialog>
      )}
    </div>
  );
};
