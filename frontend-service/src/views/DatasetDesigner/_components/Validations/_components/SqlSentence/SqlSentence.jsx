import { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { SqlHelp } from './_components/SqlHelp';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const SqlSentence = ({ creationFormState, isBusinessDataflow, onSetSqlSentence, level }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isChangedSqlSentence, setIsChangedSqlSentence] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);
  const [previousSqlSentence, setPreviousSqlSentence] = useState('');

  useEffect(() => {
    return () => onSetSqlSentence('sqlSentence', '');
  }, []);

  useEffect(() => {
    setPreviousSqlSentence(creationFormState.candidateRule?.sqlSentence);
  }, []);

  const levelTypes = {
    FIELD: 'field',
    ROW: 'row',
    TABLE: 'dataset'
  };

  const getHelpByLevel = level => {
    if (level === levelTypes.FIELD) {
      return resourcesContext.messages['sqlSentenceHelpField'];
    } else if (level === levelTypes.ROW) {
      return resourcesContext.messages['sqlSentenceHelpRow'];
    } else {
      return resourcesContext.messages['sqlSentenceHelpTable'];
    }
  };

  const onClickInfoButton = () => {
    setIsVisibleInfoDialog(true);
  };

  const onHideInfoDiaog = () => {
    setIsVisibleInfoDialog(false);
  };

  const onCCButtonClick = () => {
    onSetSqlSentence('sqlSentence', `${creationFormState.candidateRule.sqlSentence} ${codeKeyword}`);
  };
  };

  const codeKeyword = isBusinessDataflow ? `${config.COMPANY_CODE_KEYWORD}` : `${config.COUNTRY_CODE_KEYWORD}`;

  return (
    <div className={styles.section}>
      <div className={styles.content}>
        <div className={styles.helpSideBar}>
          <SqlHelp onSetSqlSentence={onSetSqlSentence} sqlSentence={creationFormState.candidateRule.sqlSentence} />
        </div>
        <div className={styles.sqlSentence}>
          <h3 className={styles.title}>
            {resourcesContext.messages['sqlSentence']}
            <Button
              className={`${styles.sqlSentenceInfoBtn} p-button-rounded p-button-secondary-transparent`}
              icon="infoCircle"
              id="infoSqlSentence"
              onClick={onClickInfoButton}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label={resourcesContext.messages['countryCodeAcronym']}
              onClick={onCCButtonClick}
              tooltip={
                isBusinessDataflow
                  ? resourcesContext.messages['matchStringCompanyTooltip']
                  : resourcesContext.messages['matchStringTooltip']
              }
              tooltipOptions={{ position: 'top' }}
            />
          </h3>
          <textarea
            id="sqlSentenceText"
            name=""
            onChange={e => {
              onSetSqlSentence('sqlSentence', e.target.value);
              setIsChangedSqlSentence(!TextUtils.areEquals(e.target.value, previousSqlSentence));
            }}
            value={creationFormState.candidateRule.sqlSentence}></textarea>
        </div>
      </div>

      {!isNil(creationFormState.candidateRule.sqlError) && !isChangedSqlSentence ? (
        <p
          className={
            styles.sqlErrorMessage
          }>{`${resourcesContext.messages['sqlErrorMessage']} ${creationFormState.candidateRule.sqlError}`}</p>
      ) : (
        <p className={styles.emptySqlErrorMessage}></p>
      )}

      {isVisibleInfoDialog && (
        <Dialog
          header={resourcesContext.messages['sqlSentenceHelpDialogTitle']}
          onHide={onHideInfoDiaog}
          style={{ maxWidth: '41vw' }}
          visible={isVisibleInfoDialog}>
          <p className={styles.levelHelp} dangerouslySetInnerHTML={{ __html: getHelpByLevel(level) }}></p>
          <p
            className={styles.note}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceHelpNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceSpatialNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceSpatialTypesNote'] }}></p>
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={
              isBusinessDataflow
                ? { __html: resourcesContext.messages['sqlSentenceCompanyCodeNote'] }
                : { __html: resourcesContext.messages['sqlSentenceCountryCodeNote'] }
            }></p>
        </Dialog>
      )}
    </div>
  );
};
