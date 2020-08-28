import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';
//'primereact/multiselect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const FieldEditor = ({
  cells,
  colsSchema,
  datasetId,
  hasWritePermissions,
  onChangePointCRS,
  onEditorKeyChange,
  onEditorSubmitValue,
  onEditorValueChange,
  onEditorValueFocus,
  onMapOpen,
  record,
  reporting,
  selectedCRS
}) => {
  const crs = [
    { label: 'WGS84', value: 'EPSG:4326' },
    { label: 'ETRS89', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG:3035' }
  ];

  const resources = useContext(ResourcesContext);
  const [codelistItemsOptions, setCodelistItemsOptions] = useState([]);
  const [codelistItemValue, setCodelistItemValue] = useState();
  const [currentCRS, setCurrentCRS] = useState(
    !isNil(selectedCRS) ? crs.filter(crsItem => crsItem.value === selectedCRS)[0] : selectedCRS
  );
  const [linkItemsOptions, setLinkItemsOptions] = useState([]);
  const [linkItemsValue, setLinkItemsValue] = useState([]);

  useEffect(() => {
    if (!isUndefined(colsSchema)) setCodelistItemsOptions(RecordUtils.getCodelistItems(colsSchema, cells.field));
    setCodelistItemValue(RecordUtils.getCellValue(cells, cells.field).toString());
    setLinkItemsValue(RecordUtils.getCellValue(cells, cells.field).toString());
  }, []);

  useEffect(() => {
    onFilter(RecordUtils.getCellValue(cells, cells.field));
  }, []);

  let fieldType = {};

  let isReadOnlyField = RecordUtils.getCellInfo(colsSchema, cells.field).readOnly;
  if (!isEmpty(record)) {
    fieldType = RecordUtils.getCellInfo(colsSchema, cells.field).type;
  }

  const onFilter = async filter => {
    const colSchema = colsSchema.filter(colSchema => colSchema.field === cells.field)[0];
    if (isNil(colSchema) || isNil(colSchema.referencedField)) {
      return;
    }

    const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;

    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      isUndefined(colSchema.referencedField.name)
        ? colSchema.referencedField.idPk
        : colSchema.referencedField.referencedField.fieldSchemaId,
      hasMultipleValues ? '' : filter
    );

    const linkItems = referencedFieldValues
      .map(referencedField => {
        return {
          itemType: referencedField.value,
          value: referencedField.value
        };
      })
      .sort((a, b) => a.value - b.value);

    if (!hasMultipleValues) {
      linkItems.unshift({
        itemType: resources.messages['noneCodelist'],
        value: ''
      });
    }
    setLinkItemsOptions(linkItems);
  };

  const formatDate = (date, isInvalidDate) => {
    if (isInvalidDate) return '';

    let d = new Date(date),
      month = '' + (d.getMonth() + 1),
      day = '' + d.getDate(),
      year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  };

  const getCodelistItemsWithEmptyOption = () => {
    const codelistsItems = RecordUtils.getCodelistItems(colsSchema, cells.field);
    codelistsItems.sort();
    codelistsItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistsItems;
  };

  const getFilter = type => {
    switch (type) {
      case 'NUMBER_INTEGER':
        return 'int';
      case 'NUMBER_DECIMAL':
      case 'POINT':
        return 'money';
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return 'num';
      case 'DATE':
        return 'date';
      case 'TEXT':
      case 'RICH_TEXT':
        return 'any';
      case 'EMAIL':
        return 'email';
      case 'PHONE':
        return 'phone';
      // case 'URL':
      //   return 'url';
      default:
        return 'any';
    }
  };

  const parsePoint = (coordinates, crs, withCRS = true) =>
    withCRS
      ? `${coordinates.split(', ')[0]}, ${coordinates.split(', ')[1]}, ${crs}`
      : `${coordinates.split(', ')[0]}, ${coordinates.split(', ')[1]}`;

  const renderField = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    // const dateCharacters = 10;
    const textCharacters = 10000;
    const richTextCharacters = 10000;
    const emailCharacters = 256;
    // const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'TEXT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
            maxLength={textCharacters}
          />
        );
      case 'RICH_TEXT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
            maxLength={richTextCharacters}
          />
        );
      case 'NUMBER_INTEGER':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            maxLength={longCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            maxLength={decimalCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'POINT':
        return (
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <InputText
              keyfilter={getFilter(type)}
              onBlur={e => onEditorSubmitValue(cells, `${e.target.value}, ${currentCRS.value}`, record)}
              onChange={e => onEditorValueChange(cells, `${e.target.value}, ${currentCRS.value}`)}
              onFocus={e => {
                e.preventDefault();
                onEditorValueFocus(cells, `${e.target.value}, ${currentCRS.value}`);
              }}
              onKeyDown={e => onEditorKeyChange(cells, e, record)}
              style={{ marginRight: '2rem' }}
              type="text"
              value={parsePoint(RecordUtils.getCellValue(cells, cells.field), currentCRS, false)}
            />
            <Dropdown
              ariaLabel={'crs'}
              appendTo={document.body}
              // className={styles.crsSwitcherSplitButton}
              options={crs}
              optionLabel="label"
              onChange={e => {
                onEditorValueChange(
                  cells,
                  parsePoint(RecordUtils.getCellValue(cells, cells.field), e.target.value.value)
                );
                setCurrentCRS(e.target.value);
                onChangePointCRS(e.target.value.value);
              }}
              placeholder="Select a CRS"
              value={currentCRS}
              style={{ width: '20%', marginRight: '1rem' }}
            />
            <Button
              className={`p-button-secondary-transparent button`}
              icon="marker"
              onClick={e => {
                if (!isNil(onMapOpen)) {
                  onMapOpen(RecordUtils.getCellValue(cells, cells.field), cells);
                }
              }}
              style={{ width: '2.357em', marginRight: '1rem' }}
              tooltip={resources.messages['selectGeographicalDataOnMap']}
              tooltipOptions={{ position: 'bottom' }}
            />
          </div>
        );
      // <Map coordinates={RecordUtils.getCellValue(cells, cells.field)}></Map>;
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'DATE':
        return (
          // <InputText
          //   keyfilter={getFilter(type)}
          //   onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
          //   onChange={e => onEditorValueChange(cells, e.target.value)}
          //   onFocus={e => {
          //     e.preventDefault();
          //     onEditorValueFocus(cells, e.target.value);
          //   }}
          //   // type="date"
          //   maxLength={dateCharacters}
          //   placeHolder="YYYY-MM-DD"
          //   value={RecordUtils.getCellValue(cells, cells.field)}
          // />
          <Calendar
            onChange={e => {
              onEditorValueChange(cells, formatDate(e.target.value, isNil(e.target.value)), record);
              onEditorSubmitValue(cells, formatDate(e.target.value, isNil(e.target.value)), record);
            }}
            onFocus={e => onEditorValueFocus(cells, formatDate(e.target.value, isNil(e.target.value)))}
            appendTo={document.body}
            dateFormat="yy-mm-dd"
            // keepInvalid={true}
            monthNavigator={true}
            value={new Date(RecordUtils.getCellValue(cells, cells.field))}
            yearNavigator={true}
            yearRange="2010:2030"
          />
        );
      case 'EMAIL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            maxLength={emailCharacters}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'URL':
        return (
          <InputText
            keyfilter={getFilter(type)}
            maxLength={urlCharacters}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
      case 'ATTACHMENT':
        return false;
      // (
      //   <InputText
      //     keyfilter={getFilter(type)}
      //     maxLength={phoneCharacters}
      //     onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
      //     onChange={e => onEditorValueChange(cells, e.target.value)}
      //     onFocus={e => {
      //       e.preventDefault();
      //       onEditorValueFocus(cells, e.target.value);
      //     }}
      //     onKeyDown={e => onEditorKeyChange(cells, e, record)}
      //     value={RecordUtils.getCellValue(cells, cells.field)}
      //   />
      // );

      case 'LINK':
        const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;
        if (hasMultipleValues) {
          return (
            <MultiSelect
              // onChange={e => onChangeForm(field, e.value)}
              appendTo={document.body}
              clearButton={false}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              maxSelectedLabels={10}
              onChange={e => {
                try {
                  setLinkItemsValue(e.value);
                  onEditorValueChange(cells, e.value);
                  onEditorSubmitValue(cells, e.value, record);
                } catch (error) {
                  console.error(error);
                }
              }}
              onFilterInputChangeBackend={onFilter}
              onFocus={e => {
                e.preventDefault();
                if (!isUndefined(codelistItemValue)) {
                  onEditorValueFocus(cells, codelistItemValue);
                }
              }}
              options={linkItemsOptions}
              optionLabel="itemType"
              value={RecordUtils.getMultiselectValues(linkItemsOptions, linkItemsValue)}
            />
          );
        } else {
          return (
            <Dropdown
              appendTo={document.body}
              currentValue={RecordUtils.getCellValue(cells, cells.field)}
              filter={true}
              filterPlaceholder={resources.messages['linkFilterPlaceholder']}
              filterBy="itemType,value"
              onChange={e => {
                setLinkItemsValue(e.target.value.value);
                onEditorValueChange(cells, e.target.value.value);
                onEditorSubmitValue(cells, e.target.value.value, record);
              }}
              onFilterInputChangeBackend={onFilter}
              onMouseDown={e => {
                onEditorValueFocus(cells, e.target.value);
              }}
              optionLabel="itemType"
              options={linkItemsOptions}
              showFilterClear={true}
              value={RecordUtils.getLinkValue(linkItemsOptions, linkItemsValue)}
            />
          );
        }
      case 'CODELIST':
        return (
          <Dropdown
            appendTo={document.body}
            onChange={e => {
              setCodelistItemValue(e.target.value.value);
              onEditorValueChange(cells, e.target.value.value);
              onEditorSubmitValue(cells, e.target.value.value, record);
            }}
            onMouseDown={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            optionLabel="itemType"
            options={getCodelistItemsWithEmptyOption()}
            value={RecordUtils.getCodelistValue(codelistItemsOptions, codelistItemValue)}
          />
        );
      case 'MULTISELECT_CODELIST':
        return (
          <MultiSelect
            appendTo={document.body}
            maxSelectedLabels={10}
            onChange={e => {
              try {
                setCodelistItemValue(e.value);
                onEditorValueChange(cells, e.value);
                onEditorSubmitValue(cells, e.value, record);
              } catch (error) {
                console.error(error);
              }
            }}
            onFocus={e => {
              e.preventDefault();
              if (!isUndefined(codelistItemValue)) {
                onEditorValueFocus(cells, codelistItemValue);
              }
            }}
            options={RecordUtils.getCodelistItems(colsSchema, cells.field)}
            optionLabel="itemType"
            value={RecordUtils.getMultiselectValues(codelistItemsOptions, codelistItemValue)}
          />
        );
      default:
        return (
          <InputText
            keyfilter={getFilter(type)}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record)}
            type="text"
            value={RecordUtils.getCellValue(cells, cells.field)}
          />
        );
    }
  };

  return !isEmpty(fieldType) && !isReadOnlyField
    ? renderField(fieldType)
    : !reporting
    ? renderField(fieldType)
    : RecordUtils.getCellValue(cells, cells.field);
};

export { FieldEditor };
