import { useContext, useEffect, useState } from 'react';

import styles from './SqlSentence.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { SqlHelp } from './_components/SqlHelp';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SqlSentence = ({ creationFormState, isBusinessDataflow, onSetSqlSentence, level }) => {
  const resources = useContext(ResourcesContext);

  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);

  useEffect(() => {
    return () => onSetSqlSentence('sqlSentence', '');
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
    onSetSqlSentence('sqlSentence', `${creationFormState.candidateRule['sqlSentence']} ${codeKeyword}`);
  };

  const codeKeyword = isBusinessDataflow ? `${config.COMPANY_CODE_KEYWORD}` : `${config.COUNTRY_CODE_KEYWORD}`;

  return (
    <div className={styles.section}>
      <div className={styles.content}>
        <div className={styles.helpSideBar}>
          <SqlHelp onSetSqlSentence={onSetSqlSentence} sqlSentence={creationFormState.candidateRule['sqlSentence']} />
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
              tooltip={
                isBusinessDataflow
                  ? resources.messages['matchStringCompanyTooltip']
                  : resources.messages['matchStringTooltip']
              }
              tooltipOptions={{ position: 'top' }}
            />
          </h3>
          <textarea
            id="sqlSentenceText"
            name=""
            onChange={e => onSetSqlSentence('sqlSentence', e.target.value)}
            value={creationFormState.candidateRule.sqlSentence}></textarea>
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
            dangerouslySetInnerHTML={
              isBusinessDataflow
                ? { __html: resources.messages['sqlSentenceCompanyCodeNote'] }
                : { __html: resources.messages['sqlSentenceCountryCodeNote'] }
            }></p>
        </Dialog>
      )}
    </div>
  );
};
