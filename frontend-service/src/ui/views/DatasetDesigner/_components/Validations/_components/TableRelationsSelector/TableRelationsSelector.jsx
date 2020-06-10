import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './TableRelationsSelector.module.scss';

import { Checkbox } from 'ui/views/_components/Checkbox';
import { Dropdown } from 'primereact/dropdown';
import { FieldRelations } from './_components/FieldRelations';
import { IfThenClause } from 'ui/views/DatasetDesigner/_components/Validations/_components/IfThenClause';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

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
  const options = [
    { label: resources.messages['fieldRelationsLabel'], value: 'fieldRelations' },
    { label: resources.messages['ifThenLabel'], value: 'ifThenClause' }
  ];
  const {
    candidateRule: { expressionType }
  } = creationFormState;
  const {
    candidateRule: { relations }
  } = creationFormState;
  const [clickedFields, setClickedFields] = useState([]);

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

  const expressionsTypeView = () => {
    if (!isEmpty(expressionType) && expressionType === 'fieldRelations') {
      return (
        <>
          <FieldRelations
            componentName={componentName}
            creationFormState={creationFormState}
            onAddNewRelation={onAddNewRelation}
            onRelationDelete={onRelationDelete}
            onRelationFieldUpdate={onRelationFieldUpdate}
            onRelationsErrors={onRelationsErrors}
            tabsChanges={tabsChanges}
            onGetFieldType={onGetFieldType}
          />
        </>
      );
    }
    return <></>;
  };
  return (
    <>
      <div className={styles.section}>
        <div className={styles.fieldsGroup}>
          <div className={styles.field}>
            <label htmlFor="dataset">{resources.messages.datasetSchema}</label>
            <Dropdown
              id={`${componentName}__dataset`}
              disabled={relations.links.length > 1}
              filterPlaceholder={resources.messages.datasetSchema}
              placeholder={resources.messages.datasetSchema}
              optionLabel="label"
              options={creationFormState.datasetSchemas}
              onChange={e => onDatasetSchemaChange(e.target.value)}
              value={creationFormState.candidateRule.relations.referencedDatasetSchema}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="table">{resources.messages.tableSchemaName}</label>
            <Dropdown
              id={`${componentName}__table`}
              disabled={relations.links.length > 1}
              filterPlaceholder={resources.messages.tableSchemaName}
              placeholder={resources.messages.tableSchemaName}
              optionLabel="label"
              options={relations.referencedTables}
              onChange={e => onReferencedTableChange(e.target.value)}
              value={relations.referencedTable}
            />
          </div>
          <div className={styles.checkbox}>
            <span>{resources.messages['pkValuesMustBeUsed']}</span>
            <Checkbox
              isChecked={creationFormState.candidateRule.relations.isDoubleReferenced}
              inputId={'isDoubleReferenced_check'}
              label="Default"
              onChange={e => onDoubleReferencedChange(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem', marginTop: '5px' }}
            />
          </div>
        </div>
      </div>
      <div className={styles.section}>{expressionsTypeView()} </div>
    </>
  );
};
