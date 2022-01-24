import { useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import utc from 'dayjs/plugin/utc';

import styles from './ValidationExpression.module.scss';

import { config } from 'conf/';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Checkbox } from 'views/_components/Checkbox';
import { Dropdown } from 'views/_components/Dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'views/_components/InputText';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';
import { TimezoneCalendar } from 'views/_components/TimezoneCalendar';

const ValidationExpression = ({
  dataflowType,
  expressionValues,
  fieldType,
  isDisabled,
  onExpressionDelete,
  onExpressionFieldUpdate,
  onExpressionGroup,
  onExpressionsErrors,
  position,
  showRequiredFields
}) => {
  dayjs.extend(utc);
  const { expressionId } = expressionValues;
  const {
    validations: { operatorTypes: operatorTypesConf, operatorByType }
  } = config;
  const inputStringMatchRef = useRef(null);
  const refDatetimeCalendar = useRef(null);

  const resourcesContext = useContext(ResourcesContext);

  const [clickedFields, setClickedFields] = useState([]);
  const [isActiveStringMatchInput, setIsActiveStringMatchInput] = useState(false);
  const [isTimezoneCalendarVisible, setIsTimezoneCalendarVisible] = useState(false);
  const [operatorTypes, setOperatorTypes] = useState([]);
  const [operatorValues, setOperatorValues] = useState([]);
  const [valueKeyFilter, setValueKeyFilter] = useState();

  useEffect(() => {
    if (inputStringMatchRef.current && isActiveStringMatchInput) {
      inputStringMatchRef.current.element.focus();
    }
    return () => {
      setIsActiveStringMatchInput(false);
    };
  }, [inputStringMatchRef.current, isActiveStringMatchInput]);

  useEffect(() => {
    if (expressionValues.operatorType) {
      setOperatorValues(operatorTypesConf[expressionValues.operatorType].values);
    }
  }, [expressionValues.operatorType]);

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
    const { operatorType } = expressionValues;

    if (operatorType === 'number' || operatorType === 'LEN') {
      setValueKeyFilter('num');
    }

    if (operatorType === 'number' && fieldType === 'NUMBER_INTEGER') {
      setValueKeyFilter('int');
    }

    if (operatorType === 'string') {
      setValueKeyFilter('');
    }
  }, [expressionValues.operatorType]);

  useEffect(() => {
    if (showRequiredFields) {
      const fieldsToAdd = [];
      ['union', 'operatorType', 'operatorValue', 'expressionValue'].forEach(field => {
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

  const printRequiredFieldError = field => {
    let conditions = false;
    if (field === 'union') {
      conditions = clickedFields.includes(field) && position !== 0 && isEmpty(expressionValues[field]);
    } else if (field === 'expressionValue') {
      conditions =
        clickedFields.includes(field) && !isNil(expressionValues[field]) && isEmpty(expressionValues[field].toString());
    } else {
      conditions = clickedFields.includes(field) && isEmpty(expressionValues[field]);
    }
    return conditions ? 'error' : '';
  };

  const onUpdateExpressionField = (key, value) => {
    checkField(key, value);
    onDeleteFromClickedFields(key);
    onExpressionFieldUpdate(expressionId, { key, value });
  };

  const onAddToClickedFields = field => {
    setTimeout(() => {
      const cClickedFields = [...clickedFields];
      if (!cClickedFields.includes(field)) {
        cClickedFields.push(field);
        setClickedFields(cClickedFields);
      }
    }, 250);
  };

  const onDeleteFromClickedFields = field => {
    const cClickedFields = [...clickedFields];
    if (cClickedFields.includes(field)) {
      cClickedFields.splice(cClickedFields.indexOf(field), 1);
      setClickedFields(cClickedFields);
    }
  };

  const onCCButtonClick = ccButtonValue => {
    onUpdateExpressionField('expressionValue', ccButtonValue);
    setIsActiveStringMatchInput(true);
  };

  const checkField = (field, fieldValue) => {
    if (field === 'year') {
      const yearInt = parseInt(fieldValue);
      if (yearInt < 1000 || yearInt > 9999) {
        onUpdateExpressionField('expressionValue', 0);
      }
    }

    if (
      expressionValues.operatorType === 'number' &&
      field === 'operatorValue' &&
      fieldValue !== 'MATCH' &&
      !Number(expressionValues.expressionValue)
    ) {
      if (!Number(fieldValue)) {
        onUpdateExpressionField('expressionValue', '');
      }
    }

    if ((expressionValues.operatorType === 'LEN' || expressionValues.operatorType === 'number') && field === 'number') {
      if (!Number(fieldValue) && Number(fieldValue) !== 0) {
        onUpdateExpressionField('expressionValue', '');
      }
    }
  };

  const calculateCalendarPanelPosition = element => {
    const {
      current: { panel }
    } = refDatetimeCalendar;

    panel.style.display = 'block';

    const inputRect = element.getBoundingClientRect();
    const panelRect = panel.getBoundingClientRect();
    const top = `${inputRect.top - panelRect.height / 2}px`;

    panel.style.top = top;
    panel.style.position = 'fixed';
  };

  const renderDatetimeCalendar = () => {
    return isTimezoneCalendarVisible ? (
      <div>
        <TimezoneCalendar
          isInModal
          onClickOutside={() => setIsTimezoneCalendarVisible(false)}
          onSaveDate={dateTime => onUpdateExpressionField('expressionValue', dateTime)}
          value={dayjs(expressionValues.expressionValue).format('YYYY-MM-DDTHH:mm:ss[Z]')}
        />
      </div>
    ) : (
      <InputText
        onFocus={() => setIsTimezoneCalendarVisible(true)}
        value={
          expressionValues.expressionValue !== ''
            ? dayjs(expressionValues.expressionValue).format('YYYY-MM-DDTHH:mm:ss[Z]')
            : ''
        }
      />
    );
  };

  const buildValueInput = () => {
    const { operatorType, operatorValue } = expressionValues;

    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return;
    }

    if (operatorType === 'date' || operatorType === 'dateTime') {
      if (operatorType === 'dateTime') {
        return renderDatetimeCalendar();
      }

      return (
        <Calendar
          appendTo={document.body}
          baseZIndex={6000}
          dateFormat="yy-mm-dd"
          inputRef={refDatetimeCalendar}
          monthNavigator={true}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          onFocus={e => {
            calculateCalendarPanelPosition(e.currentTarget);
          }}
          placeholder="YYYY-MM-DD"
          readOnlyInput={false}
          value={expressionValues.expressionValue}
          yearNavigator={true}></Calendar>
      );
    }
    if (operatorType === 'day') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          max={32}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages['value']}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'string') {
      if (operatorValue === 'MATCH') {
        const ccButtonValue = `${expressionValues.expressionValue}${TextByDataflowTypeUtils.getKeyByDataflowType(
          dataflowType,
          'sqlSentenceCodeKeyWord'
        )}`;
        return (
          <span className={styles.inputStringMatch}>
            <InputText
              disabled={isDisabled}
              id="expressionValueStringMatch"
              onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
              placeholder={resourcesContext.messages['value']}
              ref={inputStringMatchRef}
              value={expressionValues.expressionValue}
            />
            <Button
              className={`${styles.ccButton} p-button-rounded p-button-secondary-transparent`}
              label={TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'qcCodeAcronymButtonLabel'
              )}
              onClick={() => onCCButtonClick(ccButtonValue)}
              tooltip={TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'qcCodeAcronymButtonTooltip'
              )}
              tooltipOptions={{ position: 'top' }}
            />
          </span>
        );
      }
    }

    if (operatorType === 'number') {
      if (operatorValue === 'MATCH') {
        return (
          <InputText
            disabled={isDisabled}
            id="expressionValueNumberMatch"
            onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
            placeholder={resourcesContext.messages['value']}
            value={expressionValues.expressionValue}
          />
        );
      }

      if (fieldType === 'NUMBER_DECIMAL') {
        return (
          <InputText
            disabled={isDisabled}
            format="false"
            id="expressionValueNumberDecimal"
            keyfilter={valueKeyFilter}
            onBlur={e => checkField('number', e.target.value)}
            onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
            placeholder={resourcesContext.messages['value']}
            value={expressionValues.expressionValue}
          />
        );
      }
      return (
        <InputText
          disabled={isDisabled}
          format="false"
          id="expressionValueNumber"
          keyfilter={valueKeyFilter}
          onBlur={e => checkField('number', e.target.value)}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages['value']}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'year' || operatorType === 'yearDateTime') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          mode="decimal"
          onBlur={e => checkField('year', e.target.value)}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages['value']}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }

    if (operatorType === 'month' || operatorType === 'monthDateTime') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          max={13}
          min={0}
          mode="decimal"
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages['value']}
          steps={0}
          useGrouping={false}
          value={expressionValues.expressionValue}
        />
      );
    }
    if (operatorType === 'LEN') {
      return (
        <InputNumber
          disabled={isDisabled}
          format={false}
          min={-1}
          onBlur={e => checkField('number', e.target.value)}
          onChange={e => onUpdateExpressionField('expressionValue', e.target.value)}
          placeholder={resourcesContext.messages['value']}
          value={expressionValues.expressionValue}
        />
      );
    }
    return (
      <InputText
        disabled={isDisabled}
        id="expressionValueDate"
        keyfilter={valueKeyFilter}
        onChange={e => {
          onUpdateExpressionField('expressionValue', e.target.value);
        }}
        placeholder={resourcesContext.messages['value']}
        value={expressionValues.expressionValue}
      />
    );
  };

  return (
    <li className={styles.expression}>
      <span className={styles.group}>
        <Checkbox
          checked={expressionValues.group}
          disabled={isDisabled}
          onChange={e => onExpressionGroup(expressionId, { key: 'group', value: e.checked })}
        />
      </span>
      <span
        className={`${styles.union} formField ${printRequiredFieldError('union')}`}
        onBlur={() => onAddToClickedFields('union')}>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled || position === 0}
          onChange={e => onUpdateExpressionField('union', e.target.value.value)}
          optionLabel="label"
          options={config.validations.logicalOperators}
          placeholder={resourcesContext.messages['union']}
          value={first(config.validations.logicalOperators.filter(option => option.value === expressionValues.union))}
        />
      </span>
      <span
        className={`${styles.operatorType} formField ${printRequiredFieldError('operatorType')}`}
        onBlur={() => onAddToClickedFields('operatorType')}>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled}
          onChange={e => {
            onUpdateExpressionField('operatorType', e.target.value.value);
          }}
          optionLabel="label"
          options={operatorTypes}
          placeholder={resourcesContext.messages['operatorType']}
          value={first(operatorTypes.filter(option => option.value === expressionValues.operatorType))}
        />
      </span>
      <span
        className={`${styles.operatorValue} formField ${printRequiredFieldError('operatorValue')}`}
        onBlur={() => onAddToClickedFields('operatorValue')}>
        <Dropdown
          appendTo={document.body}
          disabled={isDisabled}
          onChange={e => onUpdateExpressionField('operatorValue', e.target.value.value)}
          optionLabel="label"
          options={operatorValues}
          placeholder={resourcesContext.messages['operator']}
          value={first(operatorValues.filter(option => option.value === expressionValues.operatorValue))}
        />
      </span>
      <span
        className={`${styles.expressionValue} formField ${printRequiredFieldError('expressionValue')}`}
        onBlur={() => onAddToClickedFields('expressionValue')}>
        {buildValueInput()}
      </span>
      <span>
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
export { ValidationExpression };
