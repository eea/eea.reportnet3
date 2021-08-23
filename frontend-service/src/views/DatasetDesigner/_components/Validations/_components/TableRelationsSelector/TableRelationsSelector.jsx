import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import first from 'lodash/first';

import styles from './TableRelationsSelector.module.scss';

import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { FieldRelations } from './_components/FieldRelations';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const TableRelationsSelector = ({
  componentName,
  creationFormState,
  onAddNewRelation,
  onDatasetSchemaChange,
  onDoubleReferencedChange,
  onExpressionTypeToggle,
  onGetFieldType,
  onReferencedTableChange,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  showRequiredFields,
  tabsChanges
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const {
    candidateRule: { expressionType }
  } = creationFormState;
  const {
    candidateRule: { relations }
  } = creationFormState;
  const [clickedFields, setClickedFields] = useState([]);
  const [referenceTableOptions, setReferenceTableOptions] = useState([]);

  useEffect(() => {
    onExpressionTypeToggle('fieldRelations');
  }, []);

  useEffect(() => {
    if (showRequiredFields) {
      const fieldsToAdd = [];
      ['referencedDatasetSchema', 'table'].forEach(field => {
        if (!clickedFields.includes(field)) fieldsToAdd.push(field);
      });
      setClickedFields([...clickedFields, ...fieldsToAdd]);
    }
  }, [showRequiredFields]);

  useEffect(() => {
    setReferenceTableOptions(
      creationFormState.candidateRule.relations.referencedDatasetSchema.code ===
        creationFormState.candidateRule.relations.originDatasetSchema
        ? creationFormState.schemaTables
        : relations.referencedTables
    );
  }, [creationFormState.candidateRule.relations.referencedDatasetSchema]);

  const expressionsTypeView = () => {
    if (!isEmpty(expressionType) && expressionType === 'fieldRelations') {
      return (
        <FieldRelations
          componentName={componentName}
          creationFormState={creationFormState}
          onAddNewRelation={onAddNewRelation}
          onGetFieldType={onGetFieldType}
          onRelationDelete={onRelationDelete}
          onRelationFieldUpdate={onRelationFieldUpdate}
          onRelationsErrors={onRelationsErrors}
          tabsChanges={tabsChanges}
        />
      );
    }
    return <div></div>;
  };

  return (
    <Fragment>
      <p className={styles.title}>{resourcesContext.messages['tableRelationsTitle']}</p>
      <div className={styles.section}>
        <div className={styles.fieldsGroup}>
          <div className={styles.field}>
            <label htmlFor="dataset">{resourcesContext.messages['targetDatasetSchema']}</label>
            <Dropdown
              appendTo={document.body}
              disabled={relations.links.length > 1}
              filterPlaceholder={resourcesContext.messages['referenceSchemaPlaceholder']}
              id={`${componentName}__dataset`}
              onChange={e => onDatasetSchemaChange(e.target.value)}
              optionLabel="label"
              options={creationFormState.datasetSchemas}
              placeholder={resourcesContext.messages['referenceSchemaPlaceholder']}
              value={first(
                creationFormState.datasetSchemas.filter(
                  option => option.code === creationFormState.candidateRule.relations.referencedDatasetSchema.code
                )
              )}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="table">{resourcesContext.messages['targetTable']}</label>
            <Dropdown
              appendTo={document.body}
              disabled={relations.links.length > 1}
              filterPlaceholder={resourcesContext.messages['referenceTablePlaceholder']}
              id={`${componentName}__table`}
              onChange={e => onReferencedTableChange(e.target.value)}
              optionLabel="label"
              options={referenceTableOptions}
              placeholder={resourcesContext.messages['referenceTablePlaceholder']}
              value={first(referenceTableOptions.filter(option => option.code === relations.referencedTable.code))}
            />
          </div>
          <div className={styles.checkbox}>
            <span>{resourcesContext.messages['datasetReferenceMustBeUsed']}</span>
            <Checkbox
              checked={creationFormState.candidateRule.relations.isDoubleReferenced}
              inputId="isDoubleReferenced_check"
              label="Default"
              onChange={e => onDoubleReferencedChange(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem', marginTop: '5px' }}
            />
            <label className="srOnly" htmlFor={'isDoubleReferenced_check'}>
              {resourcesContext.messages['datasetReferenceMustBeUsed']}
            </label>
          </div>
        </div>
      </div>
      <div className={styles.section}>{expressionsTypeView()} </div>
    </Fragment>
  );
};
