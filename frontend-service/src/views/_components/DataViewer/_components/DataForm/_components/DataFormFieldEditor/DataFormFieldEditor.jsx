import { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import dayjs from 'dayjs';
import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import proj4 from 'proj4';
import utc from 'dayjs/plugin/utc';

import styles from './DataFormFieldEditor.module.scss';

import { Button } from 'views/_components/Button';
import { Calendar } from 'views/_components/Calendar';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { InputTextarea } from 'views/_components/InputTextarea';
import { Map } from 'views/_components/Map';
import { MultiSelect } from 'views/_components/MultiSelect';
import { TimezoneCalendar } from 'views/_components/TimezoneCalendar';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { mapReducer } from './_functions/Reducers/mapReducer';

import { MapUtils, RecordUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';

const DataFormFieldEditor = ({
  autoFocus,
  column,
  datasetId,
  datasetSchemaId,
  editing = false,
  field,
  fieldValue = '',
  isConditional = false,
  isConditionalChanged = false,
  isSaving,
  isVisible,
  onChangeForm,
  onCheckCoordinateFieldsError,
  onConditionalChange,
  records,
  reporting,
  type
}) => {
  const crs = [
    { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    { label: 'ETRS89 - 4258', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89 - 3035', value: 'EPSG:3035' }
  ];

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);
  const linkDropdownRef = useRef(null);
  const multiDropdownRef = useRef(null);
  const pointRef = useRef(null);
  const refCalendar = useRef(null);
  const refDatetimeCalendar = useRef(null);
  const textAreaRef = useRef(null);

  dayjs.extend(utc);

  const fieldEmptyPointValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;

  const [columnWithLinks, setColumnWithLinks] = useState([]);
  const [isLoadingData, setIsLoadingData] = useState(false);
  const [isTimezoneCalendarVisible, setIsTimezoneCalendarVisible] = useState(false);
  const [map, dispatchMap] = useReducer(mapReducer, {
    currentCRS:
      fieldValue !== '' && type === 'POINT'
        ? crs.find(crsItem => crsItem.value === JSON.parse(fieldValue).properties.srid) || {
            label: 'WGS84 - 4326',
            value: 'EPSG:4326'
          }
        : { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    isMapDisabled: false,
    isMapOpen: false,
    mapCoordinates: '',
    newPoint: '',
    newPointCRS: { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    showCoordinateError: false
  });

  const { areEquals } = TextUtils;

  useEffect(() => {
    if (!isUndefined(fieldValue)) {
      if ((type === 'LINK' || type === 'EXTERNAL_LINK') && editing) {
        onLoadColsSchema(column.pkHasMultipleValues ? '' : fieldValue);
      }
      if (type === 'POINT') {
        dispatchMap({
          type: 'TOGGLE_MAP_DISABLED',
          payload: !MapUtils.checkValidCoordinates(
            fieldValue !== '' ? JSON.parse(fieldValue).geometry.coordinates.join(', ') : '',
            true
          )
        });
      }
    }
  }, []);

  useEffect(() => {
    if (isConditionalChanged) {
      if (isConditional) {
        onConditionalChange(field);
      }
      if (!isNil(linkDropdownRef.current)) {
        linkDropdownRef.current.clearFilter();
      }
    }
  }, [records.editedRecord, records.newRecord, isConditionalChanged]);

  useEffect(() => {
    if (isVisible && autoFocus) {
      if (inputRef.current) {
        inputRef.current.element.focus();
      } else if (textAreaRef.current) {
        textAreaRef.current.element.focus();
      } else if (refCalendar.current) {
        refCalendar.current.inputElement.focus();
        // } else if (refDatetimeCalendar.current) {
        //   refDatetimeCalendar.current.inputElement.focus();
      } else if (dropdownRef.current) {
        dropdownRef.current.focusInput.focus();
      } else if (multiDropdownRef.current) {
        multiDropdownRef.current.focusInput.focus();
      } else if (pointRef.current) {
        pointRef.current.element.focus();
      } else if (linkDropdownRef.current) {
        linkDropdownRef.current.focusInput.focus();
      }
    }
  }, [
    linkDropdownRef.current,
    multiDropdownRef.current,
    pointRef.current,
    dropdownRef.current,
    refCalendar.current,
    // refDatetimeCalendar.current,
    textAreaRef.current,
    inputRef.current,
    isVisible,
    records.totalRecords
  ]);

  useEffect(() => {
    if (areEquals('LINK', type) || areEquals('EXTERNAL_LINK', type)) {
      if (fieldValue === '') {
        if (!isNil(linkDropdownRef.current)) {
          linkDropdownRef.current.clearFilter();
        }
      }
    }
  }, [fieldValue]);

  useEffect(() => {
    onCheckCoordinateFieldsError(field, map.showCoordinateError);
  }, [map.showCoordinateError]);

  const onFilter = async filter => onLoadColsSchema(filter);

  const onLoadColsSchema = async filter => {
    const inmColumn = { ...column };
    const linkItems = await getLinkItemsWithEmptyOption(
      filter,
      type,
      column.referencedField,
      inmColumn.pkHasMultipleValues
    );
    inmColumn.linkItems = linkItems;
    setColumnWithLinks(inmColumn);
  };

  const onMapOpen = coordinates => {
    dispatchMap({ type: 'OPEN_MAP', payload: { coordinates } });
  };

  const onSavePoint = (coordinates, crs) => {
    if (coordinates !== '') {
      const inmMapGeoJson = cloneDeep(fieldValue !== '' ? fieldValue : fieldEmptyPointValue);
      const parsedInmMapGeoJson = JSON.parse(inmMapGeoJson);
      parsedInmMapGeoJson.geometry.coordinates = MapUtils.parseCoordinates(coordinates);
      parsedInmMapGeoJson.properties.srid = crs.value;
      onChangeForm(field, JSON.stringify(parsedInmMapGeoJson));
    }
    dispatchMap({ type: 'SAVE_MAP_COORDINATES', payload: { crs } });
  };

  const onSelectPoint = (coordinates, selectedCrs) => {
    const filteredCRS = crs.filter(crsItem => crsItem.value === selectedCrs)[0];
    dispatchMap({ type: 'SET_MAP_NEW_POINT', payload: { coordinates, filteredCRS } });
  };

  const changePoint = (geoJson, coordinates, crs, withCRS = true) => {
    if (geoJson !== '') {
      let coords = coordinates;
      geoJson.geometry.type = 'Point';
      if (withCRS) {
        coords = projectCoordinates(coordinates, crs.value);
        geoJson.geometry.coordinates = coords;
        geoJson.properties.srid = crs.value;
      } else {
        geoJson.geometry.coordinates = MapUtils.parseCoordinates(
          coordinates.replace(', ', ',').split(','),
          MapUtils.checkValidCoordinates(coords)
        );
      }
      dispatchMap({ type: 'TOGGLE_MAP_DISABLED', payload: !MapUtils.checkValidCoordinates(coords, true) });
      dispatchMap({ type: 'DISPLAY_COORDINATE_ERROR', payload: !MapUtils.checkValidCoordinates(coords, true) });
      return JSON.stringify(geoJson);
    }
  };

  const getLinkItemsWithEmptyOption = async (filter, type, referencedField, hasMultipleValues) => {
    if (isNil(type) || (!areEquals(type, 'LINK') && !areEquals(type, 'EXTERNAL_LINK')) || isNil(referencedField)) {
      return [];
    }

    if (isNil(datasetSchemaId)) {
      const metadata = await DatasetService.getMetadata(datasetId);
      datasetSchemaId = metadata.datasetSchemaId;
    }

    const conditionalField = editing
      ? records.editedRecord.dataRow.find(
          r => first(Object.keys(r.fieldData)) === referencedField.masterConditionalFieldId
        )
      : records.newRecord.dataRow.find(
          r => first(Object.keys(r.fieldData)) === referencedField.masterConditionalFieldId
        );

    const conditionalFieldValue = !isNil(conditionalField)
      ? conditionalField.fieldData?.type === 'MULTISELECT_CODELIST'
        ? Array.isArray(conditionalField.fieldData[conditionalField.fieldData.fieldSchemaId])
          ? conditionalField.fieldData[conditionalField.fieldData.fieldSchemaId]?.join('; ')
          : conditionalField.fieldData[conditionalField.fieldData.fieldSchemaId]?.replace('; ', ';').replace(';', '; ')
        : conditionalField.fieldData[conditionalField.fieldData.fieldSchemaId]
      : '';
    try {
      setIsLoadingData(true);
      const referencedFieldValues = await DatasetService.getReferencedFieldValues(
        datasetId,
        field,
        filter,
        conditionalFieldValue,
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
      return linkItems;
    } catch (error) {
      console.error('DataFormFieldEditor - getLinkItemsWithEmptyOption.', error);
      notificationContext.add({
        type: 'GET_REFERENCED_LINK_VALUES_ERROR'
      });
    } finally {
      setIsLoadingData(false);
    }
  };

  const projectCoordinates = (coordinates, newCRS) => {
    return MapUtils.checkValidCoordinates(coordinates)
      ? proj4(proj4(map.currentCRS.value), proj4(newCRS), coordinates)
      : coordinates;
  };

  const renderCodelistDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        disabled={(column.readOnly && reporting) || isSaving}
        onChange={e => {
          onChangeForm(field, e.target.value.value, isConditional);
        }}
        optionLabel="itemType"
        options={RecordUtils.getCodelistItemsWithEmptyOption(column, resourcesContext.messages['noneCodelist'])}
        ref={dropdownRef}
        value={RecordUtils.getCodelistValue(
          RecordUtils.getCodelistItemsWithEmptyOption(column, resourcesContext.messages['noneCodelist']),
          fieldValue
        )}
      />
    );
  };

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        appendTo={document.body}
        disabled={(column.readOnly && reporting) || isSaving}
        maxSelectedLabels={10}
        onChange={e => onChangeForm(field, e.value, isConditional)}
        optionLabel="itemType"
        options={column.codelistItems
          .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
          .map(codelistItem => {
            return { itemType: codelistItem, value: codelistItem };
          })}
        ref={multiDropdownRef}
        style={{ height: '34px' }}
        value={RecordUtils.getMultiselectValues(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
        valuesSeparator=";"
      />
    );
  };

  const getMaxCharactersByType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
    const datetimeCharacters = 20;
    const textCharacters = 10000;
    const richTextCharacters = 10000;
    const emailCharacters = 256;
    const phoneCharacters = 256;
    const urlCharacters = 5000;

    switch (type) {
      case 'NUMBER_INTEGER':
        return longCharacters;
      case 'NUMBER_DECIMAL':
        return decimalCharacters;
      case 'POINT':
        return textCharacters;
      case 'DATE':
        return dateCharacters;
      case 'DATETIME':
        return datetimeCharacters;
      case 'TEXT':
      case 'TEXTAREA':
        return textCharacters;
      case 'RICH_TEXT':
        return richTextCharacters;
      case 'EMAIL':
        return emailCharacters;
      case 'PHONE':
        return phoneCharacters;
      case 'URL':
        return urlCharacters;
      default:
        return null;
    }
  };

  const renderFieldEditor = () =>
    type === 'CODELIST' ? (
      renderCodelistDropdown(field, fieldValue)
    ) : type === 'MULTISELECT_CODELIST' ? (
      renderMultiselectCodelist(field, fieldValue)
    ) : type === 'LINK' || type === 'EXTERNAL_LINK' ? (
      renderLinkDropdown(field, fieldValue)
    ) : type === 'DATE' ? (
      renderCalendar(field, fieldValue)
    ) : type === 'DATETIME' ? (
      renderDatetimeCalendar(field, fieldValue)
    ) : type === 'POINT' ? (
      renderMapType(field, fieldValue)
    ) : type === 'ATTACHMENT' ? (
      renderAttachment(field, fieldValue)
    ) : ['POLYGON', 'LINESTRING', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(type) ? (
      renderComplexGeometries()
    ) : type === 'TEXTAREA' ? (
      renderTextarea(field, fieldValue)
    ) : (
      <InputText
        disabled={(column.readOnly && reporting) || isSaving}
        id={field}
        keyfilter={RecordUtils.getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        name={column.header}
        onChange={e => onChangeForm(field, e.target.value, isConditional)}
        ref={inputRef}
        style={{ width: '60%' }}
        type="text"
        value={fieldValue}
      />
    );

  const renderAttachment = () => {
    return false;
  };

  const renderCalendar = (field, fieldValue) => {
    return (
      <Calendar
        appendTo={document.body}
        baseZIndex={9999}
        dateFormat="yy-mm-dd"
        disabled={(column.readOnly && reporting) || isSaving}
        inputRef={refCalendar}
        monthNavigator={true}
        onChange={e =>
          onChangeForm(field, RecordUtils.formatDate(e.target.value, isNil(e.target.value)), isConditional)
        }
        style={{ width: '50%' }}
        value={new Date(RecordUtils.formatDate(fieldValue, isNil(fieldValue)))}
        yearNavigator={true}
        yearRange="1900:2100"
      />
    );
  };

  const renderDatetimeCalendar = (field, fieldValue) => {
    return (
      <div>
        {isTimezoneCalendarVisible ? (
          <>
            <Button
              className={'p-button-rounded p-button-secondary-transparent'}
              icon="cancel"
              onClick={() => setIsTimezoneCalendarVisible(false)}
            />
            <TimezoneCalendar
              isDisabled={(column.readOnly && reporting) || isSaving}
              isInModal
              onSaveDate={dateTime => onChangeForm(field, dateTime.format('YYYY-MM-DDTHH:mm:ss[Z]'), isConditional)}
              parentRef={refDatetimeCalendar}
              value={
                fieldValue !== ''
                  ? dayjs(fieldValue).utc().format('YYYY-MM-DDTHH:mm:ss[Z]')
                  : new Date().toISOString().split('T')[0]
              }
            />
          </>
        ) : (
          <InputText
            onFocus={e => {
              setIsTimezoneCalendarVisible(true);
            }}
            ref={refDatetimeCalendar}
            value={fieldValue}
          />
        )}
      </div>
    );
  };

  const renderComplexGeometries = () => {
    return false;
  };

  const renderLinkDropdown = (field, fieldValue) => {
    if (column.pkHasMultipleValues) {
      return (
        <MultiSelect
          appendTo={document.body}
          clearButton={false}
          disabled={(column.readOnly && reporting) || isSaving || isLoadingData}
          filter={true}
          filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
          isLoadingData={isLoadingData}
          maxSelectedLabels={10}
          onChange={e => onChangeForm(field, e.value, isConditional)}
          onFilterInputChangeBackend={onFilter}
          onFocus={() => {
            if (isEmpty(columnWithLinks.linkItems)) {
              onLoadColsSchema('');
            }
          }}
          optionLabel="itemType"
          options={columnWithLinks.linkItems}
          ref={linkDropdownRef}
          value={RecordUtils.getMultiselectValues(
            columnWithLinks.linkItems,
            !Array.isArray(fieldValue) ? fieldValue.replace('; ', ';').split(';') : fieldValue
          )}
          valuesSeparator=";"
        />
      );
    } else {
      return (
        <Dropdown
          appendTo={document.body}
          currentValue={fieldValue}
          disabled={(column.readOnly && reporting) || isSaving || isLoadingData}
          filter={true}
          filterPlaceholder={resourcesContext.messages['linkFilterPlaceholder']}
          isLoadingData={isLoadingData}
          onChange={e => {
            onChangeForm(field, e.target.value.value, isConditional);
          }}
          onFilterInputChangeBackend={onFilter}
          optionLabel="itemType"
          options={columnWithLinks.linkItems}
          ref={linkDropdownRef}
          showFilterClear={true}
          value={RecordUtils.getLinkValue(columnWithLinks.linkItems, fieldValue)}
        />
      );
    }
  };

  const renderMap = () => (
    <Map
      geoJson={fieldValue}
      geometryType={'POINT'}
      hasLegend={true}
      onSelectPoint={onSelectPoint}
      selectedCRS={map.currentCRS.value}></Map>
  );

  const renderMapType = (field, fieldValue) => (
    <div>
      <div className={styles.pointEpsgWrapper}>
        <label className={styles.epsg}>{resourcesContext.messages['coords']}</label>
        <InputText
          className={`${styles.pointInput} ${map.showCoordinateError && styles.pointInputError}`}
          disabled={(column.readOnly && reporting) || isSaving}
          id="coordinates"
          keyfilter={RecordUtils.getFilter(type)}
          onBlur={e =>
            onChangeForm(
              field,
              changePoint(
                JSON.parse(fieldValue !== '' ? fieldValue : fieldEmptyPointValue),
                e.target.value,
                map.currentCRS.value,
                false
              )
            )
          }
          onChange={e =>
            onChangeForm(
              field,
              changePoint(
                JSON.parse(fieldValue !== '' ? fieldValue : fieldEmptyPointValue),
                e.target.value,
                map.currentCRS.value,
                false
              )
            )
          }
          ref={pointRef}
          type="text"
          value={fieldValue !== '' ? JSON.parse(fieldValue).geometry.coordinates : ''}
        />
        {map.showCoordinateError && (
          <span className={styles.pointError}>{resourcesContext.messages['wrongCoordinate']}</span>
        )}
      </div>

      <div className={styles.pointEpsgWrapper}>
        <label className={styles.epsg}>{resourcesContext.messages['epsg']}</label>
        <Dropdown
          appendTo={document.body}
          ariaLabel={'crs'}
          className={styles.epsgSwitcher}
          disabled={map.isMapDisabled}
          onChange={e => {
            onChangeForm(
              field,
              changePoint(
                JSON.parse(fieldValue !== '' ? fieldValue : fieldEmptyPointValue),
                JSON.parse(fieldValue).geometry.coordinates,
                e.target.value
              )
            );
            dispatchMap({ type: 'SET_MAP_CRS', payload: { crs: e.target.value } });
          }}
          optionLabel="label"
          options={crs}
          placeholder="Select a CRS"
          value={map.currentCRS}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          disabled={map.isMapDisabled}
          icon="marker"
          onClick={() => onMapOpen(fieldValue)}
          tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );

  const renderTextarea = (field, fieldValue) => (
    <InputTextarea
      collapsedHeight={75}
      disabled={(column.readOnly && reporting) || isSaving}
      id={field}
      keyfilter={RecordUtils.getFilter(type)}
      maxLength={getMaxCharactersByType(type)}
      onChange={e => onChangeForm(field, e.target.value, isConditional)}
      ref={textAreaRef}
      style={{ width: '60%' }}
      value={fieldValue}
    />
  );

  const saveMapCoordinatesDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className={`p-button-animated-blink ${styles.saveButton}`}
        icon={'check'}
        label={resourcesContext.messages['save']}
        onClick={() => onSavePoint(map.newPoint, map.newPointCRS)}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => {
          dispatchMap({
            type: 'CANCEL_SAVE_MAP_NEW_POINT',
            payload: { newPointCRS: map.currentCRS.value }
          });
        }}
      />
    </div>
  );

  return (
    <Fragment>
      {renderFieldEditor()}
      {map.isMapOpen && (
        <Dialog
          blockScroll={false}
          className={'map-data'}
          dismissableMask={false}
          footer={saveMapCoordinatesDialogFooter}
          header={resourcesContext.messages['geospatialData']}
          modal={true}
          onHide={() => dispatchMap({ type: 'TOGGLE_MAP_VISIBILITY', payload: false })}
          visible={map.isMapOpen}>
          <div className="p-grid p-fluid">{renderMap()}</div>
        </Dialog>
      )}
    </Fragment>
  );
};

export { DataFormFieldEditor };
