import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';
import { SqlHelp } from './_components/SqlHelp';
import { SqlInputTextArea } from './_components/SqlHelp/_components/SqlInputTextArea';
import { TrafficLight } from 'views/_components/TrafficLight';
import { Dropdown } from 'views/_components/Dropdown';

import { ValidationService } from 'services/ValidationService';
import { RepresentativeService } from 'services/RepresentativeService';
import { AddOrganizationsService } from 'services/AddOrganizationsService';
import { config } from 'conf';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const SqlSentence = ({
  bigData,
  creationFormState,
  dataflowType,
  datasetId,
  dataflowId,
  level,
  onSetSqlSentence
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [columns, setColumns] = useState();
  const [hasValidationError, setHasValidationError] = useState(false);
  const [isEvaluateSqlSentenceLoading, setIsEvaluateSqlSentenceLoading] = useState(false);
  const [isSqlErrorDialogVisible, setIsSqlErrorDialogVisible] = useState(false);
  const [isSqlErrorVisible, setIsSqlErrorVisible] = useState(false);
  const [isValidatingQuery, setIsValidatingQuery] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);
  const [isVisibleSqlSentenceValidationDialog, setIsVisibleSqlSentenceValidationDialog] = useState(false);
  const [sqlResponse, setSqlResponse] = useState(null);
  const [sqlSentenceCost, setSqlSentenceCost] = useState(0);
  const [validationErrorMessage, setValidationErrorMessage] = useState('');
  const [isViewUpdated, setIsViewUpdated] = useState(false);

  const [providers, setProviders] = useState([]);
  const [selectedProvider, setSelectedProvider] = useState(null);
  const [isLoadingProviders, setIsLoadingProviders] = useState(false);

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

  useEffect(() => {
    if (dataflowId) {
      fetchProviders();
    }
  }, [dataflowId]);

  const fetchProviders = async () => {
    try {
      setIsLoadingProviders(true);
      const representativesData = await RepresentativeService.getRepresentatives(dataflowId);

      let dataProviderGroup = representativesData?.group;

      // If no group found, fetch and use the first available group based on dataflow type
      if (!dataProviderGroup?.dataProviderGroupId && dataflowType) {
        let groups = [];

        if (dataflowType === config.dataflowType.REPORTING.value) {
          const allProviderGroups = await AddOrganizationsService.getProviderGroups();
          groups = allProviderGroups.filter(
            group => group.dataProviderGroupId === 2 || group.dataProviderGroupId === 8
          );
        } else if (
          dataflowType === config.dataflowType.CITIZEN_SCIENCE.value ||
          dataflowType === config.dataflowType.BUSINESS.value
        ) {
          const response = await RepresentativeService.getGroupOrganizations();
          groups = response.data;
        } else {
          const response = await RepresentativeService.getGroupCountries();
          groups = response.data;
        }

        dataProviderGroup = groups[0];
      }

      if (dataProviderGroup?.dataProviderGroupId) {
        const dataProvidersData = await RepresentativeService.getDataProviders(dataProviderGroup);

        const formattedProviders = dataProvidersData.map(provider => ({
          id: provider.dataProviderId,
          label: provider.label,
          code: provider.code
        }));

        setProviders(formattedProviders);
      } else {
        setProviders([]);
      }
    } catch (error) {
      console.error('SqlSentence - fetchProviders.', error);
      setProviders([]);
    } finally {
      setIsLoadingProviders(false);
    }
  };

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
      icon="cancel"
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
    if (isEvaluateSqlSentenceLoading) {
      return (
        <div className={`${styles.sqlSentenceCostWrapper} ${styles.spinnerWrapper}`}>
          <Spinner className={styles.spinner} />
        </div>
      );
    } else {
      if (sqlSentenceCost !== 0 && !isNil(sqlSentenceCost)) {
        return (
          <div className={styles.sqlSentenceCostWrapper}>
            <TrafficLight className={styles.trafficLightSize} sqlSentenceCost={sqlSentenceCost} />
          </div>
        );
      }
    }
  };

  const generateValidationDialogContent = () => {
    if (columns.length === 0) {
      return <h3 className={styles.noDataMessage}>{resourcesContext.messages['noData']}</h3>;
    }

    return (
      <DataTable autoLayout initialOverflowX value={sqlResponse}>
        {columns}
      </DataTable>
    );
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
      const { data } = await ValidationService.viewUpdated(datasetId);
      setIsViewUpdated(data);
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

  const runSqlSentenceAsProvider = async () => {
    if (!selectedProvider) {
      notificationContext.add({ type: 'SELECT_PROVIDER_ERROR' }, true);
      return;
    }

    const invalidSql = /\b(limit|offset)\s*\d*$|--.*$/i.test(creationFormState?.candidateRule?.sqlSentence?.trim());

    if (invalidSql) {
      setIsSqlErrorDialogVisible(true);
      return;
    }

    setIsValidatingQuery(true);
    try {
      const showInternalFields = true;
      const response = await ValidationService.runSqlRuleAsProvider(
        datasetId,
        creationFormState.candidateRule.sqlSentence,
        showInternalFields,
        selectedProvider.code
      );
      setSqlResponse(response);
      const { data } = await ValidationService.viewUpdated(datasetId);
      setIsViewUpdated(data);
    } catch (error) {
      console.error('SqlSentence - runSqlSentenceAsProvider.', error);
      if (error.response?.status === 400 || error.response?.status === 422) {
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

  const renderIsViewUpdatedMessage = () => {
    if (!isViewUpdated) {
      return (
        <div className={styles.IsViewUpdatedMessage}>
          <p>{resourcesContext.messages['isViewUpdated']}</p>
        </div>
      );
    }
  };

  return (
    <div className={styles.section}>
      <div className={styles.content}>
        <div className={styles.helpSideBar}>
          <SqlHelp onSetSqlSentence={onSetSqlSentence} sqlSentence={creationFormState.candidateRule.sqlSentence} />
        </div>
        <div className={styles.sqlSentence}>
          <div className={styles.title}>
            <div className={styles.titleHeader}>
              <h3 style={{ margin: 0 }}>
                {bigData ? resourcesContext.messages['sqlSentenceBigData'] : resourcesContext.messages['sqlSentence']}
              </h3>
              <Button
                className={`${styles.sqlSentenceInfoBtn} p-button-rounded p-button-secondary-transparent`}
                icon="infoCircle"
                id="infoSqlSentence"
                onClick={onClickInfoButton}
                tooltip={resourcesContext.messages['sqlSentenceInfoTooltip']}
              />
            </div>

            <div className={styles.controlsStack}>
              <Button
                className={`${styles.validateButton} p-button-rounded p-button-secondary-transparent`}
                disabled={
                  isNil(creationFormState.candidateRule.sqlSentence) ||
                  isEmpty(creationFormState.candidateRule.sqlSentence)
                }
                icon="clock"
                iconClasses={styles.validateSqlSentenceIcon}
                label={resourcesContext.messages['evaluateSql']}
                onClick={() => {
                  const invalidSql = /\b(limit|offset)\s*\d*$|--.*$/i.test(
                    creationFormState?.candidateRule?.sqlSentence?.trim()
                  );

                  invalidSql ? setIsSqlErrorDialogVisible(true) : onEvaluateSqlSentence();
                }}
              />
              {renderSqlSentenceCost()}
              <Button
                className={`${styles.runButton} p-button-rounded p-button-secondary-transparent`}
                disabled={
                  isNil(creationFormState.candidateRule.sqlSentence) ||
                  isEmpty(creationFormState.candidateRule.sqlSentence) ||
                  isValidatingQuery
                }
                icon={isValidatingQuery ? 'spinnerAnimate' : 'play'}
                label={resourcesContext.messages['runSql']}
                onClick={() => {
                  const invalidSql = /\b(limit|offset)\s*\d*$|--.*$/i.test(
                    creationFormState?.candidateRule?.sqlSentence?.trim()
                  );

                  invalidSql ? setIsSqlErrorDialogVisible(true) : runSqlSentence();
                }}
              />
              <div className={styles.providerGroup}>
                <Dropdown
                  appendTo={document.body}
                  className={styles.providerDropdown}
                  disabled={isLoadingProviders || providers.length === 0}
                  onChange={e => setSelectedProvider(e.value)}
                  optionLabel="label"
                  options={providers}
                  optionValue="id"
                  placeholder={
                    isLoadingProviders
                      ? resourcesContext.messages['loading']
                      : providers.length === 0
                      ? resourcesContext.messages['noProvidersAvailable']
                      : resourcesContext.messages['selectProvider']
                  }
                  value={selectedProvider}
                />
                <Button
                  className={`${styles.runButton} p-button-rounded p-button-secondary-transparent`}
                  disabled={
                    isNil(creationFormState.candidateRule.sqlSentence) ||
                    isEmpty(creationFormState.candidateRule.sqlSentence) ||
                    isValidatingQuery ||
                    !selectedProvider
                  }
                  icon={isValidatingQuery ? 'spinnerAnimate' : 'play'}
                  label={resourcesContext.messages['runSqlAsProvider']}
                  onClick={runSqlSentenceAsProvider}
                />
              </div>
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
            </div>
          </div>
          <SqlInputTextArea
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
      <div className={styles.errorSectionWrapper}>
        <div className={styles.errorSpacer}></div>
        {renderErrorMessage()}
      </div>
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
            dangerouslySetInnerHTML={{ __html: resourcesContext.messages['sqlSentenceSemicolonReplacementNote'] }}
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

      {isSqlErrorDialogVisible && (
        <Dialog
          className={styles.dialog}
          header={resourcesContext.messages['sqlErrorDialogHeader']}
          onHide={() => setIsSqlErrorDialogVisible(false)}
          style={{ width: '600px' }}
          visible={isSqlErrorDialogVisible}>
          {resourcesContext.messages['sqlErrorMessage']}
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
          {renderIsViewUpdatedMessage()}
        </Dialog>
      )}
    </div>
  );
};
