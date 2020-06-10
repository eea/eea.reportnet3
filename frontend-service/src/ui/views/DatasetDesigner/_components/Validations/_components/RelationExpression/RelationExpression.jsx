import React, { useState, useEffect, useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './RelationExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'ui/views/_components/Button';
import { Dropdown } from 'primereact/dropdown';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import isNil from 'lodash/isNil';

const RelationExpression = ({
  expressionValues,
  isDisabled,
  layout,
  onRelationDelete,
  onRelationFieldUpdate,
  onRelationsErrors,
  originFields,
  position,
  referencedFields,
  showRequiredFields
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const { linkId } = expressionValues;
  const [clickedFields, setClickedFields] = useState([]);

  useEffect(() => {
    // console.log({ showRequiredFields, clickedFields });
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
        onBlur={() => onAddToClickedFields('originField')}
        className={`${styles.tableField} formField ${printRequiredFieldError('originField')}`}>
        <Dropdown
          disabled={isDisabled}
          onChange={e => onUpdateRelationExpressionField('originField', e.value)}
          optionLabel="label"
          options={originFields}
          placeholder={resourcesContext.messages.originField}
          value={expressionValues.originField}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('referencedField')}
        className={`${styles.tableField} formField ${printRequiredFieldError('referencedField')}`}>
        <Dropdown
          disabled={isDisabled}
          onChange={e => onUpdateRelationExpressionField('referencedField', e.value)}
          optionLabel="label"
          options={referencedFields}
          placeholder={resourcesContext.messages.referencedField}
          value={expressionValues.referencedField}
        />
      </span>

      <span>
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
export { RelationExpression };
