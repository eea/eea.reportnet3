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
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { MetadataUtils, RecordUtils } from 'ui/views/_functions/Utils';
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
  datasetSchemaId,
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
    { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    { label: 'ETRS89 - 4258', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89 - 3035', value: 'EPSG:3035' }
  ];

  const fieldEmptyPointValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;

  const resources = useContext(ResourcesContext);
  const [codelistItemsOptions, setCodelistItemsOptions] = useState([]);
  const [codelistItemValue, setCodelistItemValue] = useState();

  const [currentCRS, setCurrentCRS] = useState(
    ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
      RecordUtils.getCellInfo(colsSchema, cells.field).type
    )
      ? RecordUtils.getCellValue(cells, cells.field) !== ''
        ? crs.filter(
            crsItem => crsItem.value === JSON.parse(RecordUtils.getCellValue(cells, cells.field)).properties.srid
          )[0]
        : { label: 'WGS84 - 4326', value: 'EPSG:4326' }
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
    const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;
    onFilter(hasMultipleValues ? '' : RecordUtils.getCellValue(cells, cells.field));
    if (
      ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
        RecordUtils.getCellInfo(colsSchema, cells.field).type
      )
    ) {
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

    if (isNil(datasetSchemaId)) {
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
      datasetSchemaId = metadata.datasetSchemaId;
    }

    const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      colSchema.field,
      // isUndefined(colSchema.referencedField.name)
      //   ? colSchema.referencedField.idPk
      //   : colSchema.referencedField.referencedField.fieldSchemaId,
      filter,
      RecordUtils.getCellValue(cells, colSchema.referencedField.masterConditionalFieldId),
      datasetSchemaId
    );

    const linkItems = referencedFieldValues
      .map(referencedField => {
        return {
          itemType: `${referencedField.value}${
            !isNil(referencedField.label) &&
            referencedField.label !== '' &&
            referencedField.label !== referencedField.value
              ? ` - ${referencedField.label}`
              : ''
          }`,
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

  const changePoint = (geoJson, coordinates, crs, withCRS = true, parseToFloat = true, checkCoordinates = true) => {
    if (geoJson !== '') {
      if (withCRS) {
        const projectedCoordinates = projectCoordinates(coordinates, crs.value);
        geoJson.geometry.coordinates = projectedCoordinates;
        geoJson.properties.srid = crs.value;
        setIsMapDisabled(!MapUtils.checkValidCoordinates(projectedCoordinates));
        return JSON.stringify(geoJson);
      } else {
        setIsMapDisabled(!MapUtils.checkValidCoordinates(coordinates));
        if (checkCoordinates) {
          geoJson.geometry.coordinates = MapUtils.checkValidCoordinates(coordinates)
            ? MapUtils.parseCoordinates(coordinates.replace(', ', ',').split(','), parseToFloat)
            : [];
        } else {
          geoJson.geometry.coordinates = MapUtils.parseCoordinates(
            coordinates.replace(', ', ',').split(','),
            parseToFloat
          );
        }

        return JSON.stringify(geoJson);
      }
    }
  };

  const changeLine = (geoJson, coordinates, crs, withCRS = true, parseToFloat = true, checkCoordinates = true) => {
    if (geoJson !== '') {
      if (withCRS) {
        const projectedCoordinates = coordinates.map(coords => projectCoordinates(coords, crs.value));
        geoJson.geometry.coordinates = projectedCoordinates;
        geoJson.properties.rsid = crs.value;
        setIsMapDisabled(!MapUtils.checkValidLine(projectedCoordinates));
        return JSON.stringify(geoJson);
      } else {
        setIsMapDisabled(!MapUtils.checkValidLine(coordinates));
        if (checkCoordinates) {
          geoJson.geometry.coordinates = MapUtils.checkValidLine(coordinates)
            ? MapUtils.parseCoordinates(coordinates.replace(', ', ',').split(','), parseToFloat)
            : [];
        } else {
          geoJson.geometry.coordinates = coordinates
            .replace('[', '')
            .replace(']', '')
            .split(',')
            .map(coord => MapUtils.parseCoordinates(coord.replace(', ', ','), parseToFloat));
        }

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
      case 'TEXTAREA':
        return (
          <InputTextarea
            collapsedHeight={75}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
            maxLength={textCharacters}
            moveCaretToEnd={true}
            onChange={e => onEditorValueChange(cells, e.target.value)}
            onFocus={e => {
              e.preventDefault();
              onEditorValueFocus(cells, e.target.value);
            }}
            onKeyDown={e => onEditorKeyChange(cells, e, record, false, '', type)}
            value={RecordUtils.getCellValue(cells, cells.field)}
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
                  true,
                  false
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
                    true,
                    false
                  )
                );
              }}
              // style={{ marginRight: '2rem' }}
              type="text"
              value={
                RecordUtils.getCellValue(cells, cells.field) !== ''
                  ? JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates
                  : ''
              }
            />
            <div className={styles.pointEpsgWrapper}>
              <label className={styles.epsg}>{resources.messages['epsg']}</label>
              <div>
                <Dropdown
                  ariaLabel={'crs'}
                  appendTo={document.body}
                  className={styles.epsgSwitcher}
                  disabled={isMapDisabled}
                  options={crs}
                  optionLabel="label"
                  onChange={e => {
                    onEditorSubmitValue(
                      cells,
                      changePoint(
                        RecordUtils.getCellValue(cells, cells.field) !== ''
                          ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
                          : JSON.parse(fieldEmptyPointValue),
                        JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates,
                        e.target.value,
                        true
                      ),
                      record
                    );
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
                      onMapOpen(RecordUtils.getCellValue(cells, cells.field), cells, type);
                    }
                  }}
                  style={{ width: '35%' }}
                  tooltip={resources.messages['selectGeographicalDataOnMap']}
                  tooltipOptions={{ position: 'bottom' }}
                />
              </div>
            </div>
          </div>
        );
      case 'LINESTRING':
      case 'MULTILINESTRING':
      case 'MULTIPOINT':
      case 'MULTIPOLYGON':
      case 'POLYGON':
        const value = RecordUtils.getCellValue(cells, cells.field);
        return (
          <div className={styles.pointWrapper}>
            <label className={isNil(value) || value === '' ? styles.nonEditableData : ''}>
              {!isNil(value) && value !== ''
                ? JSON.parse(value).geometry.coordinates.join(', ')
                : resources.messages['nonEditableData']}
            </label>
            <div className={styles.pointEpsgWrapper}>
              {!isNil(value) && value !== '' && <label className={styles.epsg}>{resources.messages['epsg']}</label>}
              <div>
                {!isNil(value) && value !== '' && <span>{currentCRS.label}</span>}
                {!isNil(value) && value !== '' && (
                  <Button
                    className={`p-button-secondary-transparent button ${styles.mapButton}`}
                    icon="marker"
                    onClick={e => {
                      if (!isNil(onMapOpen)) {
                        onMapOpen(value, cells, type);
                      }
                    }}
                    style={{ width: '35%' }}
                    tooltip={resources.messages['selectGeographicalDataOnMap']}
                    tooltipOptions={{ position: 'bottom' }}
                  />
                )}
              </div>
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
