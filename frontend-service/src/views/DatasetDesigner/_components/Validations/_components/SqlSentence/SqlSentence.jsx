import { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { InputTextarea } from 'views/_components/InputTextarea';
import { SqlHelp } from './_components/SqlHelp';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const SqlSentence = ({ creationFormState, dataflowType, onSetSqlSentence, level }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isSqlErrorVisible, setIsSqlErrorVisible] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);
  const [isVisibleSqlSentenceValidationDialog, setIsVisibleSqlSentenceValidationDialog] = useState(false);

  useEffect(() => {
    if (!isNil(creationFormState.candidateRule.sqlError) && !isNil(creationFormState.candidateRule.sqlSentence)) {
      setIsSqlErrorVisible(true);
    }
  }, []);

  useEffect(() => {
    if (isSqlErrorVisible) {
      setIsSqlErrorVisible(false);
    }
  }, [creationFormState.candidateRule.sqlSentence]);

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

  const onHideInfoDialog = () => {
    setIsVisibleInfoDialog(false);
  };

  const onCCButtonClick = () => {
    onSetSqlSentence(
      `${creationFormState.candidateRule.sqlSentence || ''} ${TextByDataflowTypeUtils.getKeyByDataflowType(
        dataflowType,
        'sqlSentenceCodeKeyWord'
      )}`
    );
  };

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
              tooltip={resourcesContext.messages['sqlSentenceInfoTooltip']}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label={TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'qcCodeAcronymButtonLabel'
              )}
              onClick={onCCButtonClick}
              tooltip={TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'qcCodeAcronymButtonTooltip'
              )}
              tooltipOptions={{ position: 'top' }}
            />
            <Button
              className={`${styles.runButton} p-button-rounded p-button-secondary-transparent`}
              disabled={
                isNil(creationFormState.candidateRule.sqlSentence) ||
                isEmpty(creationFormState.candidateRule.sqlSentence)
              }
              icon="play"
              label={'Run SQL'}
              onClick={() => {
                setIsVisibleSqlSentenceValidationDialog(true);
              }}
            />
          </h3>
          <InputTextarea
            className={`p-inputtextarea`}
            id="sqlSentenceText"
            name=""
            onChange={event => onSetSqlSentence(event.target.value)}
            value={creationFormState.candidateRule.sqlSentence}></InputTextarea>
        </div>
      </div>

      {isSqlErrorVisible ? (
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
          onHide={onHideInfoDialog}
          style={{ maxWidth: '41vw' }}
          visible={isVisibleInfoDialog}>
          <p className={styles.levelHelp} dangerouslySetInnerHTML={{ __html: getHelpByLevel(level) }} />
          <p
            className={styles.note}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceHelpNote'] }}
          />
          <p
            className={styles.note}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceDatetimeNote'] }}
          />
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceSpatialNote'] }}
          />
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceSpatialTypesNote'] }}
          />
          <p
            className={styles.levelHelp}
            dangerouslySetInnerHTML={{
              __html: TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'sqlSentenceKeyWordNote'
              )
            }}
          />
        </Dialog>
      )}
    </div>
  );
};
