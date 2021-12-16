import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { InputTextarea } from 'views/_components/InputTextarea';
import { Spinner } from 'views/_components/Spinner';
import { SqlHelp } from './_components/SqlHelp';

import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const SqlSentence = ({ creationFormState, dataflowType, datasetId, level, onSetSqlSentence }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [columns, setColumns] = useState();
  const [hasValidationError, setHasValidationError] = useState(false);
  const [isSqlErrorVisible, setIsSqlErrorVisible] = useState(false);
  const [isEvaluateSqlSentenceLoading, setIsEvaluateSqlSentenceLoading] = useState(false);
  const [isValidatingQuery, setIsValidatingQuery] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);
  const [isVisibleSqlSentenceValidationDialog, setIsVisibleSqlSentenceValidationDialog] = useState(false);
  const [sqlResponse, setSqlResponse] = useState(null);
  const [sqlSentenceCost, setSqlSentenceCost] = useState(0);
  const [validationErrorMessage, setValidationErrorMessage] = useState('');

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

  useEffect(() => {
    if (!isNil(sqlResponse) && sqlResponse.length > 0) {
      setColumns(generateColumns());
      setIsVisibleSqlSentenceValidationDialog(true);
    } else if (!isNil(sqlResponse) && sqlResponse.length === 0) {
      setColumns(sqlResponse);
      setIsVisibleSqlSentenceValidationDialog(true);
    }
  }, [sqlResponse]);

  useEffect(() => {
    if (creationFormState.candidateRule.sqlSentenceCost !== 0)
      setSqlSentenceCost(creationFormState.candidateRule.sqlSentenceCost);
  }, [creationFormState.candidateRule.sqlSentenceCost]);

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

  const generateColumns = () => {
    const [firstRow] = sqlResponse;
    const columnData = Object.keys(firstRow).map(key => ({ field: key, header: key.replace('*', '.') }));

    return columnData.map(col => <Column field={col.field} header={col.header} key={col.field} />);
  };

  const sqlSentenceValidationDialogFooter = (
    <Button
      className="p-button-secondary p-button-right-aligned"
      icon={'cancel'}
      label={resourcesContext.messages['close']}
      onClick={() => setIsVisibleSqlSentenceValidationDialog(false)}
    />
  );

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
      setSqlSentenceCost(0);
      setIsEvaluateSqlSentenceLoading(true);
      const { data } = await ValidationService.evaluateSqlSentence(
        datasetId,
        creationFormState.candidateRule.sqlSentence
      );
      setSqlSentenceCost(data);
    } catch (error) {
      console.error('SqlSentence - onEvaluateSqlSentence.', error);
      if (error.response.status === 400 || error.response.status === 422) {
        setValidationErrorMessage(error.response.data.message);
        setHasValidationError(true);
      } else {
        notificationContext.add({ type: 'EVALUATE_SQL_SENTENCE_ERROR' }, true);
      }
    } finally {
      setIsEvaluateSqlSentenceLoading(false);
    }
  };

  const renderSqlSentenceCost = () => {
    const getColor = cost => {
      if (cost < config.SQL_SENTENCE_LOW_COST) {
        return 'green';
      } else if (cost < config.SQL_SENTENCE_HIGH_COST && cost > config.SQL_SENTENCE_LOW_COST) {
        return 'yellow';
      } else {
        return 'red';
      }
    };

    if (isEvaluateSqlSentenceLoading) {
      return (
        <div className={`${styles.sqlSentenceCostWrapper} ${styles.spinnerWrapper}`}>
          <Spinner className={styles.spinner} />
        </div>
      );
    } else {
      if (sqlSentenceCost !== 0 && !isNil(sqlSentenceCost)) {
        const color = getColor(sqlSentenceCost);

        return (
          <div className={`${styles.sqlSentenceCostWrapper} ${styles.trafficLight}`}>
            <div className={color === 'green' ? styles.greenLightSignal : ''} key="green"></div>
            <div className={color === 'yellow' ? styles.yellowLightSignal : ''} key="yellow"></div>
            <div className={color === 'red' ? styles.redLightSignal : ''} key="red"></div>
          </div>
        );
      }
    }
  };

  const generateValidationDialogContent = () => {
    if (columns.length === 0) {
      return <h3 className={styles.noDataMessage}>{resourcesContext.messages['noData']}</h3>;
    }

    return <DataTable value={sqlResponse}>{columns}</DataTable>;
  };

  const runSqlSentence = async () => {
    setIsValidatingQuery(true);
    try {
      const showInternalFields = true;
      const response = await ValidationService.runSqlRule(
        datasetId,
        creationFormState.candidateRule.sqlSentence,
        showInternalFields
      );
      setSqlResponse(response);
    } catch (error) {
      console.error('SqlSentence - runSqlSentence.', error);
      if (error.response.status === 400 || error.response.status === 422) {
        setValidationErrorMessage(error.response.data.message);
        setHasValidationError(true);
      } else {
        notificationContext.add({ type: 'VALIDATE_SQL_ERROR' }, true);
        setIsVisibleSqlSentenceValidationDialog(false);
      }
    } finally {
      setIsValidatingQuery(false);
    }
  };

  const renderErrorMessage = () => {
    if (hasValidationError) {
      return <p className={styles.sqlErrorMessage}>{validationErrorMessage}</p>;
    } else if (isSqlErrorVisible) {
      return <p className={styles.sqlErrorMessage}>{creationFormState.candidateRule.sqlError}</p>;
    }
    return <p className={styles.emptySqlErrorMessage}></p>;
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
                isEmpty(creationFormState.candidateRule.sqlSentence) ||
                isValidatingQuery
              }
              icon={isValidatingQuery ? 'spinnerAnimate' : 'play'}
              label={resourcesContext.messages['runSql']}
              onClick={runSqlSentence}
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
            className={`p-inputtextarea ${hasValidationError || isSqlErrorVisible ? styles.hasError : ''}`}
            id="sqlSentenceText"
            name=""
            onChange={event => {
              onSetSqlSentence(event.target.value);
            }}
            onFocus={() => setHasValidationError(false)}
            value={creationFormState.candidateRule.sqlSentence}
          />
        </div>
      </div>

      {renderErrorMessage()}

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
        <Dialog
          className={columns.length > 0 ? styles.validationDialogMaxWidth : ''}
          footer={sqlSentenceValidationDialogFooter}
          header={resourcesContext.messages['sqlSentenceValidationDialogTitle']}
          onHide={() => setIsVisibleSqlSentenceValidationDialog(false)}
          visible={isVisibleSqlSentenceValidationDialog}>
          {generateValidationDialogContent()}
        </Dialog>
      )}
    </div>
  );
};
