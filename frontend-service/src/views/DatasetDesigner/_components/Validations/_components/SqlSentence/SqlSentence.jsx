import { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';

import styles from './SqlSentence.module.scss';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { SqlHelp } from './_components/SqlHelp';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const SqlSentence = ({ creationFormState, dataflowType, onSetSqlSentence, level }) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isSqlErrorVisible, setIsSqlErrorVisible] = useState(false);
  const [isVisibleInfoDialog, setIsVisibleInfoDialog] = useState(false);

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

  const onHideInfoDiaog = () => {
    setIsVisibleInfoDialog(false);
  };

  const onCCButtonClick = () => {
    onSetSqlSentence(`${creationFormState.candidateRule.sqlSentence} ${getCodeKeyword()}`);
  };

  const getCodeKeyword = () => {
    switch (dataflowType) {
      case config.dataflowType.BUSINESS.value:
        return `${config.COMPANY_CODE_KEYWORD}`;

      case config.dataflowType.CITIZEN_SCIENCE.value:
        return `${config.ORGANIZATION_CODE_KEYWORD}`;

      default:
        return `${config.COUNTRY_CODE_KEYWORD}`;
    }
  };

  const getButtonTooltipMessage = () => {
    switch (dataflowType) {
      case config.dataflowType.BUSINESS.value:
        return resourcesContext.messages['matchStringCompanyTooltip'];

      case config.dataflowType.CITIZEN_SCIENCE.value:
        return resourcesContext.messages['matchStringOrganizationTooltip'];

      default:
        return resourcesContext.messages['matchStringTooltip'];
    }
  };

  const getInfoText = () => {
    switch (dataflowType) {
      case config.dataflowType.BUSINESS.value:
        return { __html: resourcesContext.messages['sqlSentenceCompanyCodeNote'] };

      case config.dataflowType.CITIZEN_SCIENCE.value:
        return { __html: resourcesContext.messages['sqlSentenceOrganizationCodeNote'] };

      default:
        return { __html: resourcesContext.messages['sqlSentenceCountryCodeNote'] };
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
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label={resourcesContext.messages['countryCodeAcronym']}
              onClick={onCCButtonClick}
              tooltip={getButtonTooltipMessage()}
              tooltipOptions={{ position: 'top' }}
            />
          </h3>
          <textarea
            id="sqlSentenceText"
            name=""
            onChange={event => onSetSqlSentence(event.target.value)}
            value={creationFormState.candidateRule.sqlSentence}></textarea>
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
          <p className={styles.levelHelp} dangerouslySetInnerHTML={getInfoText()}></p>
        </Dialog>
      )}
    </div>
  );
};
