import { useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import uniqueId from 'lodash/uniqueId';

import styles from './FieldEditor.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Coordinates } from 'views/_components/Coordinates';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { MultiSelect } from 'views/_components/MultiSelect';
import { TimezoneCalendar } from 'views/_components/TimezoneCalendar';
import { TooltipButton } from 'views/_components/TooltipButton';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'views/_functions/Utils';
import { MapUtils } from 'views/_functions/Utils/MapUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

const FieldEditor = ({
  cells,
  colsSchema,
  datasetId,
  datasetSchemaId,
  onChangePointCRS,
  onCoordinatesMoreInfoClick,
  onEditorKeyChange,
  onEditorSubmitValue,
  onEditorValueChange,
  onEditorValueFocus,
  onMapOpen,
  record,
  reporting
}) => {
  const refDatetimeCalendar = useRef(null);
  const refCalendar = useRef(null);
  const crs = [
    { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    { label: 'ETRS89 - 4258', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89 - 3035', value: 'EPSG:3035' }
  ];

  const fieldEmptyPointValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [codelistItemsOptions, setCodelistItemsOptions] = useState([]);
  const [codelistItemValue, setCodelistItemValue] = useState();
  const [currentCRS, setCurrentCRS] = useState(
    ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
      RecordUtils.getCellInfo(colsSchema, cells.field).type
    )
      ? RecordUtils.getCellValue(cells, cells.field) !== ''
        ? crs.find(
            crsItem => crsItem.value === JSON.parse(RecordUtils.getCellValue(cells, cells.field)).properties.srid
          ) || { label: 'WGS84 - 4326', value: 'EPSG:4326' }
        : { label: 'WGS84 - 4326', value: 'EPSG:4326' }
      : {}
  );
  const [isCalendarVisible, setIsCalendarVisible] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(false);
  const [isMapDisabled, setIsMapDisabled] = useState(
    RecordUtils.getCellInfo(colsSchema, cells.field).type === 'POINT'
      ? !MapUtils.checkValidCoordinates(
          RecordUtils.getCellValue(cells, cells.field) !== ''
            ? JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates.join(', ')
            : '',
          true
        )
      : true
  );
  const [linkItemsOptions, setLinkItemsOptions] = useState([]);
  const [linkItemsValue, setLinkItemsValue] = useState([]);

  const [dateTime, setDateTime] = useState();

  const { areEquals } = TextUtils;

  const calendarId = uniqueId();
  const calendarWithDatetimeId = uniqueId();

  useEffect(() => {
    if (!isUndefined(colsSchema)) setCodelistItemsOptions(getCodelistItemsWithEmptyOption());
    setCodelistItemValue(
      Array.isArray(RecordUtils.getCellValue(cells, cells.field))
        ? RecordUtils.getCellValue(cells, cells.field).join(';')
        : RecordUtils.getCellValue(cells, cells.field).toString()
    );
    setLinkItemsValue(
      Array.isArray(RecordUtils.getCellValue(cells, cells.field))
        ? RecordUtils.getCellValue(cells, cells.field).join(';')
        : RecordUtils.getCellValue(cells, cells.field).toString()
    );
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
      const metadata = await DatasetService.getMetadata(datasetId);
      datasetSchemaId = metadata.datasetSchemaId;
    }

    const fieldInfo = RecordUtils.getCellInfo(colsSchema, cells.field);
    const referencedFieldInfo = RecordUtils.getCellInfo(colsSchema, colSchema.referencedField.masterConditionalFieldId);
    const hasMultipleValues = fieldInfo.pkHasMultipleValues;

    try {
      setIsLoadingData(true);
      const conditionalValue = RecordUtils.getCellValue(cells, colSchema.referencedField.masterConditionalFieldId);
      const referencedFieldValues = await DatasetService.getReferencedFieldValues(
        datasetId,
        colSchema.field,
        filter,
        referencedFieldInfo?.type === 'MULTISELECT_CODELIST'
          ? Array.isArray(conditionalValue)
            ? conditionalValue.join('; ')
            : conditionalValue.replace('; ', ';').replace(';', '; ')
          : conditionalValue,
        datasetSchemaId,
        100
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
          itemType: resourcesContext.messages['noneCodelist'],
          value: ''
        });
      }

      if (referencedFieldValues.length > 99) {
        linkItems[linkItems.length - 1] = {
          disabled: true,
          itemType: resourcesContext.messages['moreElements'],
          value: ''
        };
      }
      setLinkItemsOptions(linkItems);
    } catch (error) {
      console.error('FieldEditor - onFilter.', error);
      notificationContext.add({ type: 'GET_REFERENCED_LINK_VALUES_ERROR' }, true);
    } finally {
      setIsLoadingData(false);
    }
  };

  const calculateCalendarPanelPosition = (element, fieldId) => {
    const idx = colsSchema.map(e => e.field).indexOf(fieldId);
    if (idx === record.dataRow?.length - 3 && !isCalendarVisible) {
      let panel = refDatetimeCalendar?.current?.panel;

      if (!panel) {
        panel = refCalendar.current.panel;
      }

      const inputRect = element.getBoundingClientRect();
      const panelRect = panel.getBoundingClientRect();
      if (panelRect.right + panelRect.width > window.innerWidth) {
        panel.style.left = `${
          inputRect.left - (Number(panelRect.width) - (Number(inputRect.right) - Number(inputRect.left)))
        }px`;
      } else {
        panel.style.left = `${inputRect.left}px`;
      }
    }
  };

  const changePoint = (geoJson, coordinates, crs, withCRS = true, parseToFloat = true, checkCoordinates = true) => {
    if (geoJson !== '') {
      geoJson.geometry.type = 'Point';
      if (withCRS) {
        const projectedCoordinates = MapUtils.projectCoordinates({ coordinates, currentCRS, newCRS: crs.value });
        geoJson.geometry.coordinates = projectedCoordinates;
        geoJson.properties.srid = crs.value;
        setIsMapDisabled(!MapUtils.checkValidCoordinates(projectedCoordinates, true));
        return JSON.stringify(geoJson);
      } else {
        setIsMapDisabled(!MapUtils.checkValidCoordinates(coordinates, true));
        if (checkCoordinates) {
          geoJson.geometry.coordinates = MapUtils.checkValidCoordinates(coordinates, true)
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

  const getCodelistItemsWithEmptyOption = () => {
    const codelistsItems = RecordUtils.getCodelistItems(colsSchema, cells.field);
    codelistsItems.sort((a, b) => a.value.localeCompare(b.value, undefined, { numeric: true, sensitivity: 'base' }));
    codelistsItems.unshift({
      itemType: resourcesContext.messages['noneCodelist'],
      value: ''
    });
    return codelistsItems;
  };

  const onCalendarBlur = e => {
    if (e.target.value !== RecordUtils.getCellValue(cells, cells.field)) {
      saveCalendarDate(e.target.value, false);
      setIsCalendarVisible(false);
    }
  };

  const onCalendarFocus = e => {
    calculateCalendarPanelPosition(e.currentTarget, cells.field);
    setIsCalendarVisible(true);
    onEditorValueFocus(cells, RecordUtils.formatDate(e.target.value, isNil(e.target.value)));
  };

  const onCoordinatesBlur = coordinates => {
    onEditorSubmitValue(
      cells,
      changePoint(
        RecordUtils.getCellValue(cells, cells.field) !== ''
          ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
          : JSON.parse(fieldEmptyPointValue),
        coordinates,
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
        coordinates,
        currentCRS.value,
        false
      )
    );
  };

  const onCoordinatesKeyDown = (e, coordinates) => {
    changePoint(
      RecordUtils.getCellValue(cells, cells.field) !== ''
        ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
        : JSON.parse(fieldEmptyPointValue),
      coordinates,
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
        coordinates,
        currentCRS.value,
        false,
        true,
        false
      )
    );
  };

  const saveCalendarDate = (inputDateValue, withDatetime) => {
    const formattedDateValue = isEmpty(inputDateValue)
      ? ''
      : !withDatetime
      ? RecordUtils.formatDate(inputDateValue, isNil(inputDateValue))
      : inputDateValue;
    const isCorrectDateFormattedValue = getIsCorrectDateFormatedValue(formattedDateValue);
    if (isCorrectDateFormattedValue || isEmpty(formattedDateValue)) {
      onEditorValueChange(cells, formattedDateValue, record);
      onEditorSubmitValue(cells, formattedDateValue, record);
    }
  };

  const saveCalendarFromKeys = (inputDateValue, withDatetime = false) => {
    setIsCalendarVisible(false);
    saveCalendarDate(inputDateValue, withDatetime);
  };

  const onSelectCalendar = (e, withDatetime = false) => {
    saveCalendarDate(!withDatetime ? dayjs(e.value).format('YYYY-MM-DD') : dayjs(e.value).format(), withDatetime);
  };

  useEffect(() => {
    if (isCalendarVisible) {
      const calendarInput = document.getElementById(calendarId);
      !isNil(calendarInput) &&
        calendarInput.addEventListener('keydown', event => {
          const {
            key,
            target: { value: inputValue }
          } = event;
          const storedValue = RecordUtils.getCellValue(cells, cells.field);

          if ((key === 'Tab' || key === 'Enter') && inputValue !== storedValue) {
            saveCalendarFromKeys(inputValue);
          }
        });
      const calendarWithDatetimeInput = document.getElementById(calendarWithDatetimeId);
      !isNil(calendarWithDatetimeInput) &&
        calendarWithDatetimeInput.addEventListener('keydown', event => {
          const {
            key,
            target: { value: inputValue }
          } = event;
          const storedValue = RecordUtils.getCellValue(cells, cells.field);

          if ((key === 'Tab' || key === 'Enter') && inputValue !== storedValue) {
            saveCalendarFromKeys(inputValue, true);
          }
        });
    }
  }, [isCalendarVisible]);

  const getIsCorrectDateFormatedValue = date => !isEmpty(date);

  const onCrsChange = crs => {
    onEditorSubmitValue(
      cells,
      changePoint(
        RecordUtils.getCellValue(cells, cells.field) !== ''
          ? JSON.parse(RecordUtils.getCellValue(cells, cells.field))
          : JSON.parse(fieldEmptyPointValue),
        JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates,
        crs,
        true
      ),
      record
    );
    onEditorValueChange(
      cells,
      changePoint(
        JSON.parse(RecordUtils.getCellValue(cells, cells.field)),
        JSON.parse(RecordUtils.getCellValue(cells, cells.field)).geometry.coordinates,
        crs
      ),
      crs
    );

    setCurrentCRS(crs);
    onChangePointCRS(crs.value);
  };

  const renderCRS = fieldValue => {
    const parsedGeoJsonData = JSON.parse(fieldValue);
    const selectedCRS = crs.find(crsItem => crsItem.value === parsedGeoJsonData.properties.srid);
    if (!isNil(selectedCRS)) {
      return selectedCRS.label;
    } else {
      return parsedGeoJsonData.properties.srid.split(':')[1];
    }
  };

  const renderEPSGInfo = (fieldValue, differentTypes, isValidJSON) => {
    if (!differentTypes) {
      return (
        <div className={styles.pointEpsgWrapper}>
          {!isNil(fieldValue) && fieldValue !== '' && isValidJSON && (
            <label className={styles.epsg}>{resourcesContext.messages['epsg']}: </label>
          )}
          {!isNil(fieldValue) && fieldValue !== '' && isValidJSON && <span>{renderCRS(fieldValue)}</span>}
        </div>
      );
    }
  };

  const renderField = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const textCharacters = 10000;
    const richTextCharacters = 10000;
    const emailCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'TEXT':
        return (
          <InputText
            id={cells.field}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={textCharacters}
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
      case 'TEXTAREA':
        return (
          <InputTextarea
            collapsedHeight={75}
            id={cells.field}
            maxLength={textCharacters}
            moveCaretToEnd={true}
            onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
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
            id={cells.field}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={richTextCharacters}
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
      case 'NUMBER_INTEGER':
        return (
          <InputText
            id={cells.field}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={longCharacters}
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
      case 'NUMBER_DECIMAL':
        return (
          <InputText
            id={cells.field}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={decimalCharacters}
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
      case 'POINT':
        return (
          <div className={styles.pointEpsgWrapper}>
            <Coordinates
              crsDisabled={isMapDisabled}
              crsOptions={crs}
              crsValue={!isNil(currentCRS) ? currentCRS : { label: 'WGS84 - 4326', value: 'EPSG:4326' }}
              id={cells.field}
              initialGeoJson={RecordUtils.getCellValue(cells, cells.field)}
              isCellEditor={true}
              onBlur={coordinates => onCoordinatesBlur(coordinates)}
              onCoordinatesMoreInfoClick={onCoordinatesMoreInfoClick}
              onCrsChange={crs => onCrsChange(crs)}
              onFocus={() => onEditorValueFocus(cells, RecordUtils.getCellValue(cells, cells.field))}
              onKeyDown={(e, value) => onCoordinatesKeyDown(e, value)}
              onMapOpen={() => onMapOpen(RecordUtils.getCellValue(cells, cells.field), cells, type)}
              xyLabels={currentCRS.value === 'EPSG:3035'}
            />
          </div>
        );
      case 'LINESTRING':
      case 'MULTILINESTRING':
      case 'MULTIPOINT':
      case 'MULTIPOLYGON':
      case 'POLYGON':
        const value = RecordUtils.getCellValue(cells, cells.field);
        let differentTypes = false;
        if (!isNil(value) && value !== '') {
          differentTypes = !areEquals(JSON.parse(value).geometry.type, type);
        }
        let isValidJSON = false;
        if (!differentTypes) {
          isValidJSON = MapUtils.checkValidJSONMultipleCoordinates(value);
        }
        return (
          <div>
            <div className={styles.pointWrapper}>
              {renderMultipleCoordinatesInfo(value, isValidJSON, differentTypes)}
              {renderEPSGInfo(value, differentTypes, isValidJSON)}
            </div>
            {!isNil(value) && value !== '' && isValidJSON && MapUtils.hasValidCRS(value, crs) && (
              <Button
                className={`p-button-secondary-transparent button ${styles.mapButton}`}
                disabled={differentTypes}
                icon="marker"
                onClick={() => {
                  if (!isNil(onMapOpen)) {
                    onMapOpen(value, cells, type);
                  }
                }}
                tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
                tooltipOptions={{ position: 'bottom' }}
              />
            )}
          </div>
        );
      case 'DATE':
        return (
          <Calendar
            appendTo={document.body}
            baseZIndex={9999}
            dateFormat="yy-mm-dd"
            inputId={calendarId}
            inputRef={refCalendar}
            monthNavigator={true}
            onBlur={onCalendarBlur}
            onFocus={onCalendarFocus}
            onSelect={onSelectCalendar}
            value={new Date(RecordUtils.getCellValue(cells, cells.field))}
            yearNavigator={true}
            yearRange="1900:2100"
          />
        );
      case 'DATETIME':
        return (
          <TimezoneCalendar
            onSaveDate={dateTimeProp => {
              setDateTime(!isNil(dateTimeProp) ? dateTimeProp : '');
              saveCalendarDate(dateTimeProp === '' ? '' : dateTimeProp.format('YYYY-MM-DDTHH:mm:ss[Z]'), true);
              document.body.click();
            }}
            value={!isNil(dateTime) ? dateTime : RecordUtils.getCellValue(cells, cells.field)}
          />
        );

      case 'EMAIL':
        return (
          <InputText
            id={cells.field}
            keyfilter={RecordUtils.getFilter(type)}
            maxLength={emailCharacters}
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
      case 'URL':
        return (
          <InputText
            id={cells.field}
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

      case 'EXTERNAL_LINK':
      case 'LINK':
        const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;
        if (hasMultipleValues) {
          return (
            <MultiSelect
              appendTo={document.body}
              clearButton={false}
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
              maxSelectedLabels={10}
              onChange={e => {
                try {
                  setLinkItemsValue(e.value);
                  onEditorValueChange(cells, e.value);
                  onEditorSubmitValue(
                    cells,
                    e.value.sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })),
                    record
                  );
                } catch (error) {
                  console.error('FieldEditor - renderField - LINK.', error);
                }
              }}
              onFilterInputChangeBackend={onFilter}
              onFocus={e => {
                e.preventDefault();
                if (!isUndefined(codelistItemValue)) {
                  onEditorValueFocus(cells, codelistItemValue);
                }
              }}
              optionLabel="itemType"
              options={linkItemsOptions}
              value={RecordUtils.getMultiselectValues(linkItemsOptions, linkItemsValue)}
              valuesSeparator=";"
            />
          );
        } else {
          return (
            <Dropdown
              appendTo={document.body}
              currentValue={RecordUtils.getCellValue(cells, cells.field)}
              disabled={isLoadingData}
              filter={true}
              filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
              isLoadingData={isLoadingData}
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
                onEditorSubmitValue(
                  cells,
                  e.value.sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })),
                  record
                );
              } catch (error) {
                console.error('FieldEditor - renderField - MULTISELECT_CODELIST.', error);
              }
            }}
            onFocus={e => {
              e.preventDefault();
              if (!isUndefined(codelistItemValue)) {
                onEditorValueFocus(cells, codelistItemValue);
              }
            }}
            optionLabel="itemType"
            options={RecordUtils.getCodelistItems(colsSchema, cells.field)}
            value={RecordUtils.getMultiselectValues(codelistItemsOptions, codelistItemValue)}
            valuesSeparator=";"
          />
        );
      default:
        return (
          <InputText
            id={cells.field}
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

  const renderFieldAsLabel = (value, type) => {
    if (cells && cells.field && !isEmpty(type)) {
      if (
        ['POINT', 'POLYGON', 'LINESTRING', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(type.toUpperCase())
      ) {
        if (value !== '') {
          const parsedJSON = JSON.parse(value);
          return `${parsedJSON.geometry.coordinates.join(', ')} - ${parsedJSON.properties.srid}`;
        } else {
          return '';
        }
      } else {
        return value;
      }
    } else {
      return '';
    }
  };

  const renderMultipleCoordinatesInfo = (value, isValidJSON, differentTypes) => {
    const infoLabelClass =
      isNil(value) || value === '' || !isValidJSON || differentTypes ? styles.nonEditableData : null;

    const getInfoLabelContent = () => {
      if (!isNil(value) && value !== '' && isValidJSON && !differentTypes) {
        return JSON.parse(value).geometry.coordinates.join(', ');
      } else {
        if (differentTypes) {
          return resourcesContext.messages['nonEditableDataDifferentTypes'];
        } else {
          if (value === '') {
            return resourcesContext.messages['nonEditableDataAndCantParse'];
          } else {
            return resourcesContext.messages['nonEditableData'];
          }
        }
      }
    };

    const renderMoreInfo = () => {
      if (value !== '') {
        return (
          <TooltipButton
            message={resourcesContext.messages['coordinatesMoreInfo']}
            onClick={() => onCoordinatesMoreInfoClick(RecordUtils.getCellValue(cells, cells.field))}
            uniqueIdentifier={`coordinates_${cells.field}`}></TooltipButton>
        );
      }
    };

    const completeCoordinates = getInfoLabelContent();

    return (
      <div>
        {isNil(infoLabelClass) && <label className={styles.epsg}>{resourcesContext.messages['coords']}</label>}
        {isNil(infoLabelClass) && renderMoreInfo()}
        <div className={styles.completeCoordinatesWrapper}>
          <label className={infoLabelClass}>{completeCoordinates}</label>
          {!isNil(infoLabelClass) && renderMoreInfo()}
        </div>
      </div>
    );
  };

  return !isEmpty(fieldType) && !isReadOnlyField ? (
    renderField(fieldType)
  ) : !reporting ? (
    renderField(fieldType)
  ) : (
    <span
      style={{
        whiteSpace: cells && cells.field && fieldType === 'TEXTAREA' ? 'pre-wrap' : 'none'
      }}>
      {renderFieldAsLabel(
        RecordUtils.getCellValue(cells, cells.field),
        RecordUtils.getCellInfo(colsSchema, cells.field).type
      )}
    </span>
  );
};

export { FieldEditor };
