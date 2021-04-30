import { useContext, useEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uuid from 'uuid';

import styles from './ComparisonExpression.module.scss';

import { config } from 'conf/';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Checkbox } from 'ui/views/_components/Checkbox/Checkbox';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'ui/views/_components/InputText';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const ComparisonExpression = ({
  expressionValues,
  isDisabled,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  onGetFieldType,
  position,
  rawTableFields,
  showRequiredFields
}) => {
  const componentName = 'fieldComparison';
  const { expressionId } = expressionValues;
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType, fieldByOperatorType }
  } = config;

  const resourcesContext = useContext(ResourcesContext);
  const inputStringMatchRef = useRef(null);

  const [clickedFields, setClickedFields] = useState([]);
  const [disabledFields, setDisabledFields] = useState({});
  const [fieldType, setFieldType] = useState(null);
  const [isActiveStringMatchInput, setIsActiveStringMatchInput] = useState(false);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [operatorValues, setOperatorValues] = useState([]);
  const [previousValue, setPreviousValue] = useState();
  const [secondFieldOptions, setSecondFieldOptions] = useState();
  const [tableFields, setTableFields] = useState([]);
  const [valueKeyFilter, setValueKeyFilter] = useState();
  const [valueTypeSelectorOptions, setValueTypeSelectorOptions] = useState([]);

  useEffect(() => {
    if (inputStringMatchRef.current && isActiveStringMatchInput) {
      inputStringMatchRef.current.element.focus();
    }
    return () => {
      setIsActiveStringMatchInput(false);
    };
  }, [inputStringMatchRef.current, isActiveStringMatchInput]);

  useEffect(() => {
    setValueTypeSelectorOptions([
      { label: 'field', value: 'field' },
      { label: 'value', value: 'value' }
    ]);
  }, []);

  useEffect(() => {
    const newDisabledFields = {};

    newDisabledFields.field1 = isDisabled;
    newDisabledFields.operatorType = isEmpty(expressionValues.field1);
    newDisabledFields.operatorValue = isEmpty(expressionValues.operatorType);
    newDisabledFields.valueTypeSelector = isEmpty(expressionValues.operatorValue);
    newDisabledFields.field2 = isEmpty(expressionValues.valueTypeSelector);

    setDisabledFields({
      ...disabledFields,
      ...newDisabledFields
    });
  }, [
    expressionValues.field1,
    expressionValues.field2,
    expressionValues.operatorType,
    expressionValues.operatorValue,
    expressionValues.valueTypeSelector,
    isDisabled
  ]);

  const isFieldInTable = tableFields => {
    const result = tableFields.filter(tField => tField.value === expressionValues.field1);
    return !isEmpty(result);
  };

  useEffect(() => {
    if (rawTableFields) {
      const parsedTableFields = rawTableFields.map(field => {
        return { label: field.label, value: field.code, type: field.type };
      });

      if (!isFieldInTable(parsedTableFields) && !isNil(expressionValues.field1) && expressionValues.field1 !== '') {
        ['union', 'field1', 'operatorType', 'operatorValue', 'valueTypeSelector', 'field2'].forEach(field => {
          onExpressionFieldUpdate(expressionId, {
            key: field,
            value: null
          });
        });
      }

      setTableFields(parsedTableFields);
      setSecondFieldOptions(parsedTableFields);
    }
  }, [rawTableFields]);

  useEffect(() => {
    if (!isEmpty(tableFields)) {
      setDisabledFields({
        ...disabledFields,
        field1: false
      });
    }
  }, [tableFields]);

  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
    if (expressionValues.operatorType === 'LEN') {
      setValueKeyFilter('num');
    }
  }, [expressionValues.operatorType]);

  useEffect(() => {
    if (!isEmpty(expressionValues.field1)) {
      setFieldType(onGetFieldType(expressionValues.field1));
    }
  }, [expressionValues.field1]);

  useEffect(() => {
    if (!isEmpty(expressionValues.field1) && !isEmpty(fieldType) && expressionValues.operatorType) {
      const field1Type = onGetFieldType(expressionValues.field1);
      let compatibleFieldTypes = fieldByOperatorType[expressionValues.operatorType];

      if (
        (field1Type === 'NUMBER_INTEGER' || field1Type === 'NUMBER_DECIMAL') &&
        expressionValues.operatorValue === 'MATCH'
      ) {
        compatibleFieldTypes = fieldByOperatorType['numberMatch'];
      }

      const allFields = tableFields.filter(field => {
        const cFieldType = onGetFieldType(field.value);
        const result = compatibleFieldTypes.includes(cFieldType);
        return result;
      });

      setSecondFieldOptions(allFields.filter(cField => cField.value !== expressionValues.field1));
    }
  }, [expressionValues.field1, expressionValues.operatorType, fieldType, tableFields, expressionValues.operatorValue]);

  useEffect(() => {
    const options = [];
    if (!isNil(fieldType)) {
      operatorByType[fieldType]?.forEach(key => {
        options.push(operatorTypesConf[key].option);
      });
    } else {
      for (let type in operatorTypesConf) {
        options.push(operatorTypesConf[type].option);
      }
    }
    setOperatorTypes(options);
  }, [fieldType]);

  useEffect(() => {
    if (showRequiredFields) {
      const fieldsToAdd = [];

      ['union', 'field1', 'operatorType', 'operatorValue', 'valueTypeSelector', 'field2'].forEach(field => {
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
      onExpressionsErrors(expressionId, true);
    } else {
      onExpressionsErrors(expressionId, false);
    }
  }, [clickedFields, showRequiredFields]);

  useEffect(() => {
    setPreviousValue(expressionValues.field2);
    return () => {
      setPreviousValue('');
    };
  }, []);

  const printRequiredFieldError = field => {
    let conditions = false;

    if (field === 'union') {
      conditions = clickedFields.includes(field) && position !== 0 && isEmpty(expressionValues[field]);
    } else if (field === 'field2' && expressionValues.valueTypeSelector === 'value') {
      conditions =
        clickedFields.includes(field) &&
        (isNil(expressionValues[field]) || isEmpty(expressionValues[field].toString()));

      if (expressionValues.operatorValue === 'IS NULL' || expressionValues.operatorValue === 'IS NOT NULL') {
        conditions = false;
      }
    } else {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field]);
    }

    return conditions ? 'error' : '';
  };

  const onUpdateExpressionField = (key, value) => {
    checkField(key, value);

    onDeleteFromClickedFields(key);

    if (key === 'field1' && value !== expressionValues.field1) {
      ['operatorType', 'operatorValue', 'valueTypeSelector', 'field2'].forEach(field => {
        onExpressionFieldUpdate(expressionId, {
          key: field,
          value: ''
        });
      });
    }

    if (key === 'valueTypeSelector' && value !== expressionValues.valueTypeSelector) {
      onExpressionFieldUpdate(expressionId, {
        key: 'field2',
        value: ''
      });
    }

    onExpressionFieldUpdate(expressionId, {
      key,
      value
    });
  };

  const onAddToClickedFields = field => {
    setTimeout(() => {
      const cClickedFields = [...clickedFields];

      if (!cClickedFields.includes(field)) {
        cClickedFields.push(field);
        setClickedFields(cClickedFields);
      }
    }, 300);
  };

  const onCCButtonClick = ccButtonValue => {
    onUpdateExpressionField('field2', ccButtonValue);
    setIsActiveStringMatchInput(true);
  };

  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];

    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };

  const checkField = (field, fieldValue) => {
    if (expressionValues.valueTypeSelector === 'value') {
      if (field === 'year') {
        const yearInt = parseInt(fieldValue);

        if (yearInt < 1000 || yearInt > 9999) {
          onUpdateExpressionField('field2', 0);
        }
      }
      if (expressionValues.operatorType === 'number' && field === 'operatorValue' && fieldValue !== 'MATCH') {
        const number = Number(fieldValue);
        if (!number) {
          onUpdateExpressionField('field2', '');
        }
      }
      if (
        (expressionValues.operatorType === 'LEN' || expressionValues.operatorType === 'number') &&
        field === 'number'
      ) {
        if (!Number(fieldValue) && Number(fieldValue) !== 0) {
          onUpdateExpressionField('field2', previousValue);
        } else {
          setPreviousValue(fieldValue);
        }
      }
    }
  };

  const getTypeField = () => {
    if (expressionValues.operatorValue === 'IS NULL' || expressionValues.operatorValue === 'IS NOT NULL') {
      return;
    }
    return (
      <span
        onBlur={() => onAddToClickedFields('valueTypeSelector')}
        className={`${styles.operatorValue} formField ${printRequiredFieldError('valueTypeSelector')}`}>
        <Dropdown
          disabled={disabledFields.valueTypeSelector}
          onChange={e => onUpdateExpressionField('valueTypeSelector', e.value)}
          optionLabel="label"
          options={valueTypeSelectorOptions}
          optionValue="value"
          placeholder={resourcesContext.messages.comparisonValueFieldSelector}
          value={expressionValues.valueTypeSelector}
        />
      </span>
    );
  };

  const getValueField = () => {
    if (expressionValues.operatorValue === 'IS NULL' || expressionValues.operatorValue === 'IS NOT NULL') {
      return;
    } else if (expressionValues.valueTypeSelector === 'value') {
      return buildValueInput();
    } else {
      return (
        <Dropdown
          disabled={disabledFields.field2}
          filterPlaceholder={resourcesContext.messages.selectField}
          id={`${componentName}__field2`}
          onChange={e => onUpdateExpressionField('field2', e.value)}
          optionLabel="label"
          options={secondFieldOptions}
          placeholder={resourcesContext.messages.selectField}
          value={expressionValues.field2}
        />
      );
    }
  };

  const buildValueInput = () => {
    const { operatorType, operatorValue, field2 } = expressionValues;

    if (operatorType === 'date') {
      return (
        <Calendar
          id={uuid.v4()}
          appendTo={document.body}
          baseZIndex={6000}
          dateFormat="yy-mm-dd"
          monthNavigator={true}
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder="YYYY-MM-DD"
          readOnlyInput={false}
          value={field2}
          yearNavigator={true}
          yearRange="1900:2500"></Calendar>
      );
    }
    if (operatorType === 'day') {
      return (
        <InputNumber
          id={uuid.v4()}
          disabled={isDisabled}
          format={false}
          max={32}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={field2}
        />
      );
    }
    if (operatorType === 'string') {
      if (operatorValue === 'MATCH') {
        const ccButtonValue = `${field2}{%R3_COUNTRY_CODE%}`;
        return (
          <span className={styles.inputStringMatch}>
            <InputText
              id={uuid.v4()}
              disabled={isDisabled}
              onChange={e => onUpdateExpressionField('field2', e.target.value)}
              placeholder={resourcesContext.messages.value}
              value={field2}
              ref={inputStringMatchRef}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label="CC"
              tooltip={resourcesContext.messages['matchStringTooltip']}
              tooltipOptions={{ position: 'top' }}
              onClick={() => onCCButtonClick(ccButtonValue)}
            />
          </span>
        );
      }
    }

    if (operatorType === 'number') {
      if (operatorValue === 'MATCH') {
        return (
          <InputText
            id={uuid.v4()}
            disabled={isDisabled}
            onChange={e => onUpdateExpressionField('field2', e.target.value)}
            placeholder={resourcesContext.messages.value}
            value={field2}
          />
        );
      }

      if (fieldType === 'NUMBER_DECIMAL') {
        return (
          <InputText
            id={uuid.v4()}
            keyfilter="num"
            disabled={isDisabled}
            format={false}
            onBlur={e => checkField('number', e.target.value)}
            onChange={e => onUpdateExpressionField('field2', e.target.value)}
            placeholder={resourcesContext.messages.value}
            value={field2}
          />
        );
      }

      return (
        <InputNumber
          id={uuid.v4()}
          disabled={isDisabled}
          format={false}
          mode="decimal"
          onBlur={e => checkField('number', e.target.value)}
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={field2}
        />
      );
    }

    if (operatorType === 'year') {
      return (
        <InputNumber
          id={uuid.v4()}
          disabled={isDisabled}
          mode="decimal"
          onBlur={e => checkField('year', e.target.value)}
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={field2}
        />
      );
    }

    if (operatorType === 'month') {
      return (
        <InputNumber
          id={uuid.v4()}
          disabled={isDisabled}
          format={false}
          max={13}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder={resourcesContext.messages.value}
          steps={0}
          useGrouping={false}
          value={field2}
        />
      );
    }

    if (operatorType === 'LEN') {
      return (
        <InputNumber
          id={uuid.v4()}
          keyfilter={valueKeyFilter}
          disabled={isDisabled}
          format={false}
          onBlur={e => checkField('number', e.target.value)}
          onChange={e => onUpdateExpressionField('field2', e.target.value)}
          placeholder={resourcesContext.messages.value}
          value={field2}
        />
      );
    }

    return (
      <InputText
        id={uuid.v4()}
        keyfilter={valueKeyFilter}
        disabled={isDisabled}
        onChange={e => {
          onUpdateExpressionField('field2', e.target.value);
        }}
        placeholder={resourcesContext.messages.value}
        value={field2}
      />
    );
  };

  return (
    <li className={styles.expression}>
      <span className={styles.group}>
        <FontAwesomeIcon icon={AwesomeIcons('link')} />
        <Checkbox
          disabled={disabledFields.union}
          isChecked={expressionValues.group}
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
        />
      </span>

      <span
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}
        onBlur={() => onAddToClickedFields('union')}>
        <Dropdown
          disabled={position === 0}
          onChange={e => onUpdateExpressionField('union', e.value)}
          optionLabel="label"
          options={config.validations.logicalOperators}
          placeholder={resourcesContext.messages.union}
          value={expressionValues.union}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('field1')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('field1')}`}>
        <Dropdown
          disabled={disabledFields.field1}
          id={`${componentName}__field1`}
          onChange={e => {
            onUpdateExpressionField('field1', e.value);
          }}
          optionLabel={'label'}
          options={tableFields}
          placeholder={resourcesContext.messages.selectField}
          value={expressionValues.field1}
        />
      </span>

      <span
        onBlur={() => onAddToClickedFields('operatorType')}
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}>
        <Dropdown
          disabled={disabledFields.operatorType}
          onChange={e => onUpdateExpressionField('operatorType', e.value)}
          optionLabel="label"
          options={operatorTypes}
          placeholder={resourcesContext.messages.operatorType}
          value={expressionValues.operatorType}
        />
      </span>
      <span
        onBlur={() => onAddToClickedFields('operatorValue')}
        className={`${styles.operatorValue} formField ${printRequiredFieldError('operatorValue')}`}>
        <Dropdown
          disabled={disabledFields.operatorValue}
          onChange={e => onUpdateExpressionField('operatorValue', e.value)}
          optionLabel="label"
          options={operatorValues}
          optionValue="value"
          placeholder={resourcesContext.messages.operator}
          value={expressionValues.operatorValue}
        />
      </span>
      {getTypeField()}
      <span
        onBlur={() => onAddToClickedFields('field2')}
        className={`formField ${styles.expressionValue} ${printRequiredFieldError('field2')}`}>
        {getValueField()}
      </span>

      <span className={`formField ${styles.deleteButtonWrap}`}>
        <Button
          className={`p-button-rounded p-button-secondary-transparent ${styles.deleteButton} p-button-animated-blink`}
          disabled={isDisabled}
          icon="trash"
          onClick={() => onExpressionDelete(expressionId)}
          type="button"
        />
      </span>
    </li>
  );
};
export { ComparisonExpression };
