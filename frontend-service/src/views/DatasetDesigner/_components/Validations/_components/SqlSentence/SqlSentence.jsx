import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { InputTextarea } from 'views/_components/InputTextarea';
import { Spinner } from 'views/_components/Spinner';
import { SqlHelp } from './_components/SqlHelp';
import { SqlSentenceValidation } from './_components/SqlSentenceValidation/SqlSentenceValidation';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const SqlSentence = ({ creationFormState, dataflowType, datasetId, level, onSetSqlSentence }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [isSqlErrorVisible, setIsSqlErrorVisible] = useState(false);
  const [isEvaluateSqlSentenceLoading, setIsEvaluateSqlSentenceLoading] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);
  const [isVisibleSqlSentenceValidationDialog, setIsVisibleSqlSentenceValidationDialog] = useState(false);
  const [sqlSentenceCost, setSqlSentenceCost] = useState(0);

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

  const onEvaluateSqlSentence = async () => {
    try {
      setIsEvaluateSqlSentenceLoading(true);
      const { data } = await ValidationService.evaluateSqlSentence(
        datasetId,
        creationFormState.candidateRule.sqlSentence
      );
      setSqlSentenceCost(data);
    } catch (error) {
      setSqlSentenceCost(0);
      console.error('SqlSentence - onEvaluateSqlSentence.', error);
      if (error.response.status === 400) {
        notificationContext.add({ type: 'EVALUATE_SQL_SENTENCE_FORMAT_ERROR' }, true);
      } else if (error.response.status === 422) {
        notificationContext.add({ type: 'EVALUATE_SQL_SENTENCE_COMMANDS_NOT_ALLOWED_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'EVALUATE_SQL_SENTENCE_ERROR' }, true);
      }
    } finally {
      setIsEvaluateSqlSentenceLoading(false);
    }
  };

  const renderSqlSentenceCost = () => {
    if (isEvaluateSqlSentenceLoading) {
      return (
        <div className={`${styles.sqlSentenceCostWrapper} ${styles.spinnerWrapper}`}>
          <Spinner className={styles.spinner} />
        </div>
      );
    } else {
      if (sqlSentenceCost !== 0) {
        return (
          <div className={`${styles.sqlSentenceCostWrapper} ${styles.trafficLight}`}>
            <div className={`${sqlSentenceCost < config.SQL_SENTENCE_LOW_COST ? styles.greenLightSignal : ''}`}></div>
            <div
              className={`${
                sqlSentenceCost < config.SQL_SENTENCE_HIGH_COST && sqlSentenceCost > config.SQL_SENTENCE_LOW_COST
                  ? styles.yellowLightSignal
                  : ''
              }`}></div>
            <div className={`${sqlSentenceCost > config.SQL_SENTENCE_HIGH_COST ? styles.redLightSignal : ''}`}></div>
          </div>
        );
      }
    }
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
              label={resourcesContext.messages['runSql']}
              onClick={() => {
                setIsVisibleSqlSentenceValidationDialog(true);
              }}
            />
            <Button
              className={`${styles.validateButton} p-button-rounded p-button-secondary-transparent`}
              disabled={
                isNil(creationFormState.candidateRule.sqlSentence) ||
                isEmpty(creationFormState.candidateRule.sqlSentence)
              }
              icon="clock"
              iconClasses={styles.validateSqlSentenceIcon}
              label={resourcesContext.messages['evaluateSql']}
              onClick={onEvaluateSqlSentence}
            />
            {renderSqlSentenceCost()}
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

      {isVisibleSqlSentenceValidationDialog && (
        <SqlSentenceValidation
          isVisibleSqlSentenceValidationDialog={isVisibleSqlSentenceValidationDialog}
          setIsVisibleSqlSentenceValidationDialog={setIsVisibleSqlSentenceValidationDialog}
          sqlSentence={creationFormState.candidateRule.sqlSentence}
        />
      )}
    </div>
  );
};
