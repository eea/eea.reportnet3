import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import proj4 from 'proj4';

import styles from './FieldEditor.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';

proj4.defs([
  ['EPSG:4258', '+proj=longlat +ellps=GRS80 +no_defs'],
  ['EPSG:3035', '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs'],
  ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs']
]);

const FieldEditor = ({
  cells,
  colsSchema,
  datasetId,
  onChangePointCRS,
  onEditorKeyChange,
  onEditorSubmitValue,
  onEditorValueChange,
  onEditorValueFocus,
  onMapOpen,
  record,
  reporting
}) => {
  const crs = [
    { label: 'WGS84', value: 'EPSG:4326' },
    { label: 'ETRS89', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG:3035' }
  ];

  const fieldEmptyPointValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`;

  const resources = useContext(ResourcesContext);
  const [codelistItemsOptions, setCodelistItemsOptions] = useState([]);
  const [codelistItemValue, setCodelistItemValue] = useState();

  const [currentCRS, setCurrentCRS] = useState(
    RecordUtils.getCellInfo(colsSchema, cells.field).type === 'POINT'
      ? RecordUtils.getCellValue(cells, cells.field) !== ''
        ? crs.filter(
            crsItem => crsItem.value === JSON.parse(RecordUtils.getCellValue(cells, cells.field)).properties.rsid
          )[0]
        : { label: 'WGS84', value: 'EPSG:4326' }
      : {}
  );
  const [isMapDisabled, setIsMapDisabled] = useState(
    RecordUtils.getCellInfo(colsSchema, cells.field).type === 'POINT'
      ? !MapUtils.checkValidCoordinates(
          RecordUtils.getCellValue(cells, cells.field) !== ''
            ? JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates.join(', ')
            : ''
        )
      : true
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
    if (RecordUtils.getCellInfo(colsSchema, cells.field).type === 'POINT') {
      onChangePointCRS(currentCRS.value);
    }
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

  const changePoint = (geoJson, coordinates, crs, withCRS = true, parseToFloat = true) => {
    if (geoJson !== '') {
      if (withCRS) {
        const projectedCoordinates = projectCoordinates(coordinates, crs.value);
        geoJson.geometry.coordinates = projectedCoordinates;
        geoJson.properties.rsid = crs.value;
        setIsMapDisabled(!MapUtils.checkValidCoordinates(projectedCoordinates));
        return JSON.stringify(geoJson);
      } else {
        setIsMapDisabled(!MapUtils.checkValidCoordinates(coordinates));
        geoJson.geometry.coordinates = MapUtils.parseCoordinates(
          coordinates.replace(', ', ',').split(','),
          parseToFloat
        );

        return JSON.stringify(geoJson);
      }
    }
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

  const projectCoordinates = (coordinates, newCRS) => {
    return proj4(proj4(currentCRS.value), proj4(newCRS), coordinates);
  };

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
            keyfilter={RecordUtils.getFilter(type)}
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
            keyfilter={RecordUtils.getFilter(type)}
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
            keyfilter={RecordUtils.getFilter(type)}
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
            keyfilter={RecordUtils.getFilter(type)}
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
          <div className={styles.pointWrapper}>
            <InputText
              keyfilter={RecordUtils.getFilter(type)}
              onBlur={e => {
                onEditorSubmitValue(
                  cells,
                  changePoint(
                    RecordUtils.getCellValue(cells, cells.field) !== ''
                      ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                      : JSON.parse(fieldEmptyPointValue),
                    e.target.value,
                    currentCRS.value,
                    false
                  ),
                  record
                );
                onEditorValueChange(
                  cells,
                  changePoint(
                    RecordUtils.getCellValue(cells, cells.field) !== ''
                      ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                      : JSON.parse(fieldEmptyPointValue),
                    e.target.value,
                    currentCRS.value,
                    false
                  )
                );
              }}
              onChange={e =>
                onEditorValueChange(
                  cells,
                  changePoint(
                    RecordUtils.getCellValue(cells, cells.field) !== ''
                      ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                      : JSON.parse(fieldEmptyPointValue),
                    e.target.value,
                    currentCRS.value,
                    false,
                    false
                  )
                )
              }
              onFocus={e => {
                e.preventDefault();
                onEditorValueFocus(cells, RecordUtils.getCellValue(cells, cells.field));
              }}
              onKeyDown={e => {
                changePoint(
                  RecordUtils.getCellValue(cells, cells.field) !== ''
                    ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                    : JSON.parse(fieldEmptyPointValue),
                  e.target.value,
                  currentCRS.value,
                  false,
                  true
                );
                onEditorKeyChange(
                  cells,
                  e,
                  record,
                  true,
                  changePoint(
                    RecordUtils.getCellValue(cells, cells.field) !== ''
                      ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                      : JSON.parse(fieldEmptyPointValue),
                    e.target.value,
                    currentCRS.value,
                    false,
                    true
                  )
                );
              }}
              // style={{ marginRight: '2rem' }}
              type="text"
              value={
                RecordUtils.getCellValue(cells, cells.field) !== ''
                  ? JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates.join(', ')
                  : ''
              }
            />
            <div className={styles.pointSridWrapper}>
              <label className={styles.srid}>{resources.messages['srid']}</label>
              <Dropdown
                ariaLabel={'crs'}
                appendTo={document.body}
                className={styles.sridSwitcher}
                disabled={isMapDisabled}
                options={crs}
                optionLabel="label"
                onChange={e => {
                  onEditorValueChange(
                    cells,
                    changePoint(
                      JSON.parse(RecordUtils.getCellValue(cells, cells.field)),
                      JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates,
                      e.target.value
                    ),
                    e.target.value
                  );
                  setCurrentCRS(e.target.value);
                  onChangePointCRS(e.target.value.value);
                }}
                placeholder="Select a CRS"
                value={currentCRS}
              />
              <Button
                className={`p-button-secondary-transparent button ${styles.mapButton}`}
                icon="marker"
                onClick={e => {
                  if (!isNil(onMapOpen)) {
                    onMapOpen(RecordUtils.getCellValue(cells, cells.field), cells);
                  }
                }}
                style={{ width: '35%' }}
                tooltip={resources.messages['selectGeographicalDataOnMap']}
                tooltipOptions={{ position: 'bottom' }}
              />
            </div>
          </div>
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
              onEditorValueChange(cells, RecordUtils.formatDate(e.target.value, isNil(e.target.value)), record);
              onEditorSubmitValue(cells, RecordUtils.formatDate(e.target.value, isNil(e.target.value)), record);
            }}
            onFocus={e => onEditorValueFocus(cells, RecordUtils.formatDate(e.target.value, isNil(e.target.value)))}
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
            keyfilter={RecordUtils.getFilter(type)}
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
            keyfilter={RecordUtils.getFilter(type)}
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
            keyfilter={RecordUtils.getFilter(type)}
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
