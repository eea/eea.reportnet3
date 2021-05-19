import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './TableRelationsSelector.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { FieldRelations } from './_components/FieldRelations';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { first } from 'lodash';

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
  const resources = useContext(ResourcesContext);
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
  }, []);

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
    return <></>;
  };

  return (
    <>
      <p className={styles.title}>{resources.messages['tableRelationsTitle']}</p>
      <div className={styles.section}>
        <div className={styles.fieldsGroup}>
          <div className={styles.field}>
            <label htmlFor="dataset">{resources.messages['targetDatasetSchema']}</label>
            <Dropdown
              appendTo={document.body}
              disabled={relations.links.length > 1}
              filterPlaceholder={resources.messages['referenceSchemaPlaceholder']}
              id={`${componentName}__dataset`}
              onChange={e => onDatasetSchemaChange(e.target.value.value)}
              optionLabel="label"
              options={creationFormState.datasetSchemas}
              placeholder={resources.messages['referenceSchemaPlaceholder']}
              value={first(
                creationFormState.datasetSchemas.filter(
                  option => option.value === creationFormState.candidateRule.relations.referencedDatasetSchema
                )
              )}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="table">{resources.messages['targetTable']}</label>
            <Dropdown
              appendTo={document.body}
              disabled={relations.links.length > 1}
              filterPlaceholder={resources.messages['referenceTablePlaceholder']}
              id={`${componentName}__table`}
              onChange={e => onReferencedTableChange(e.target.value)}
              optionLabel="label"
              options={referenceTableOptions}
              placeholder={resources.messages['referenceTablePlaceholder']}
              value={first(referenceTableOptions.filter(option => option.value === relations.referencedTable))}
            />
          </div>
          <div className={styles.checkbox}>
            <span>{resources.messages['datasetReferenceMustBeUsed']}</span>
            <Checkbox
              inputId={'isDoubleReferenced_check'}
              isChecked={creationFormState.candidateRule.relations.isDoubleReferenced}
              label="Default"
              onChange={e => onDoubleReferencedChange(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem', marginTop: '5px' }}
            />
            <label className="srOnly" htmlFor={'isDoubleReferenced_check'}>
              {resources.messages['datasetReferenceMustBeUsed']}
            </label>
          </div>
        </div>
      </div>
      <div className={styles.section}>{expressionsTypeView()} </div>
    </>
  );
};
