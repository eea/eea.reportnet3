import { useContext } from 'react';

import styles from './FieldRelations.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ValidationExpressionSelector } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionSelector';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { checkComparisonRelation } from 'ui/views/DatasetDesigner/_components/Validations/_functions/Utils/checkComparisonRelation';

export const FieldRelations = ({
  componentName,
  creationFormState,
  onAddNewRelation,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  onGetFieldType,
  tabsChanges
}) => {
  const resourcesContext = useContext(ResourcesContext);
  return (
    <div className={styles.section}>
      <ul className={styles.list}>
        {creationFormState.candidateRule.relations.links &&
          creationFormState.candidateRule.relations.links.map((link, i) => (
            <ValidationExpressionSelector
              expressionType={creationFormState.candidateRule.expressionType}
              expressionValues={link}
              isDisabled={creationFormState.areRulesDisabled}
              key={link.linkId}
              onGetFieldType={onGetFieldType}
              onRelationDelete={onRelationDelete}
              onRelationFieldUpdate={onRelationFieldUpdate}
              onRelationsErrors={onRelationsErrors}
              originFields={creationFormState.tableFields}
              position={i}
              referencedFields={creationFormState.candidateRule.relations.referencedFields}
              showRequiredFields={tabsChanges.expression}
            />
          ))}
      </ul>
      <div className={styles.expressionsActionsBtns}>
        <Button
          className="p-button-primary p-button-text-icon-left"
          disabled={checkComparisonRelation(creationFormState.candidateRule.relations.links)}
          icon="plus"
          id={`${componentName}__addRelation`}
          label={resourcesContext.messages.addNewRelation}
          onClick={() => onAddNewRelation()}
          type="button"
        />
      </div>
    </div>
  );
};
