import { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './RelationExpression.module.scss';

import { Button } from 'views/_components/Button';
import { Dropdown } from 'views/_components/Dropdown';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import isNil from 'lodash/isNil';

export const RelationExpression = ({
  expressionValues,
  isDisabled,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  originFields,
  referencedFields,
  showRequiredFields
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { linkId } = expressionValues;
  const [clickedFields, setClickedFields] = useState([]);

  useEffect(() => {
    if (showRequiredFields) {
      const fieldsToAdd = [];
      ['originField', 'referencedField'].forEach(field => {
        if (!clickedFields.includes(field)) fieldsToAdd.push(field);
      });
      setClickedFields([...clickedFields, ...fieldsToAdd]);
    }
  }, [showRequiredFields]);

  useEffect(() => {
    let errors = false;
    clickedFields.forEach(clickedField => {
      if (printRequiredFieldError(clickedField) === 'error') {
        errors = true;
      }
    });
    if (errors) {
      onRelationsErrors(linkId, true);
    } else {
      onRelationsErrors(linkId, false);
    }
  }, [clickedFields, showRequiredFields]);

  const printRequiredFieldError = field => {
    let conditions = false;
    if (field === 'originField' || field === 'referencedField') {
      conditions = clickedFields.includes(field) && expressionValues[field] === '';
    } else if (field === 'expressionValue') {
      conditions =
        clickedFields.includes(field) && !isNil(expressionValues[field]) && isEmpty(expressionValues[field].toString());
    } else {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field]);
    }
    return conditions ? 'error' : '';
  };

  const onUpdateRelationExpressionField = (key, value) => {
    onDeleteFromClickedFields(key);
    onRelationFieldUpdate(linkId, {
      key,
      value
    });
  };

  const onAddToClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (!cClickedFields.includes(field)) {
      cClickedFields.push(field);
      setClickedFields(cClickedFields);
    }
  };
  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };

  return (
    <li className={styles.expression}>
      <span
        className={`${styles.tableField} formField ${printRequiredFieldError('originField')}`}
        onBlur={() => onAddToClickedFields('originField')}>
        <label htmlFor="originField">{resourcesContext.messages['originField']}</label>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled}
          onChange={e => onUpdateRelationExpressionField('originField', e.value)}
          optionLabel="label"
          options={originFields}
          placeholder={resourcesContext.messages['originFieldPlaceholder']}
          value={expressionValues.originField}
        />
      </span>
      <span
        className={`${styles.tableField} formField ${printRequiredFieldError('referencedField')}`}
        onBlur={() => onAddToClickedFields('referencedField')}>
        <label htmlFor="referencedField">{resourcesContext.messages['referencedField']}</label>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled}
          onChange={e => onUpdateRelationExpressionField('referencedField', e.value)}
          optionLabel="label"
          options={referencedFields}
          placeholder={resourcesContext.messages['referencedFieldPlaceholder']}
          value={expressionValues.referencedField}
        />
      </span>

      <span className={styles.deleteButtonWrap}>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
          disabled={isDisabled}
          icon="trash"
          onClick={() => onRelationDelete(linkId)}
          type="button"
        />
      </span>
    </li>
  );
};
