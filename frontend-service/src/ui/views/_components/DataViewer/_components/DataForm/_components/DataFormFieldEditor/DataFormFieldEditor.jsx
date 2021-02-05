import React, { useContext, useEffect, useReducer, useRef, useState } from 'react';

// import isEmpty from 'lodash/isEmpty';
import cloneDeep from 'lodash/cloneDeep';
import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import proj4 from 'proj4';

import styles from './DataFormFieldEditor.module.scss';

// import { DatasetConfig } from 'conf/domain/model/Dataset';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { Map } from 'ui/views/_components/Map';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { mapReducer } from './_functions/Reducers/mapReducer';

import { MapUtils, MetadataUtils, RecordUtils, TextUtils } from 'ui/views/_functions/Utils';

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

  const resources = useContext(ResourcesContext);

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);
  const linkDropdownRef = useRef(null);
  const multiDropdownRef = useRef(null);
  const pointRef = useRef(null);
  const refCalendar = useRef(null);
  const textAreaRef = useRef(null);

  const fieldEmptyPointValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;

  const [columnWithLinks, setColumnWithLinks] = useState([]);
  const [map, dispatchMap] = useReducer(mapReducer, {
    currentCRS:
      fieldValue !== '' && type === 'POINT'
        ? crs.filter(crsItem => crsItem.value === JSON.parse(fieldValue).properties.srid)[0]
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
      if (type === 'LINK' && editing) {
        onLoadColsSchema(column.pkHasMultipleValues ? '' : fieldValue);
      }
      if (type === 'POINT') {
        dispatchMap({
          type: 'TOGGLE_MAP_DISABLED',
          payload: !MapUtils.checkValidCoordinates(
            fieldValue !== '' ? JSON.parse(fieldValue).geometry.coordinates.join(', ') : ''
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
    textAreaRef.current,
    inputRef.current,
    isVisible,
    records.totalRecords
  ]);

  useEffect(() => {
    if (areEquals('LINK', type)) {
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
      dispatchMap({ type: 'TOGGLE_MAP_DISABLED', payload: !MapUtils.checkValidCoordinates(coords) });
      dispatchMap({ type: 'DISPLAY_COORDINATE_ERROR', payload: !MapUtils.checkValidCoordinates(coords, true) });
      return JSON.stringify(geoJson);
    }
  };

  const getLinkItemsWithEmptyOption = async (filter, type, referencedField, hasMultipleValues) => {
    if (isNil(type) || !areEquals(type, 'LINK') || isNil(referencedField)) {
      return [];
    }

    if (isNil(datasetSchemaId)) {
      const metadata = await MetadataUtils.getDatasetMetadata(datasetId);
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
      ? conditionalField.fieldData[conditionalField.fieldData.fieldSchemaId]
      : '';
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      field,
      // isUndefined(referencedField.name) ? referencedField.idPk : referencedField.referencedField.fieldSchemaId,
      filter,
      conditionalFieldValue,
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
    return linkItems;
  };

  const projectCoordinates = (coordinates, newCRS) => {
    return proj4(proj4(map.currentCRS.value), proj4(newCRS), coordinates);
  };

  const renderCodelistDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        disabled={column.readOnly && reporting}
        onChange={e => {
          onChangeForm(field, e.target.value.value, isConditional);
        }}
        optionLabel="itemType"
        options={RecordUtils.getCodelistItemsWithEmptyOption(column, resources.messages['noneCodelist'])}
        ref={dropdownRef}
        value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
      />
    );
  };

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        addSpaceCommaSeparator={true}
        appendTo={document.body}
        disabled={column.readOnly && reporting}
        maxSelectedLabels={10}
        onChange={e => onChangeForm(field, e.value, isConditional)}
        options={column.codelistItems
          .sort((a, b) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' }))
          .map(codelistItem => {
            return { itemType: codelistItem, value: codelistItem };
          })}
        optionLabel="itemType"
        ref={multiDropdownRef}
        style={{ height: '34px' }}
        value={RecordUtils.getMultiselectValues(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
      />
    );
  };

  const getMaxCharactersByType = type => {
    const longCharacters = 20;
    const decimalCharacters = 40;
    const dateCharacters = 10;
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
    ) : type === 'LINK' ? (
      renderLinkDropdown(field, fieldValue)
    ) : type === 'DATE' ? (
      renderCalendar(field, fieldValue)
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
        disabled={column.readOnly && reporting}
        id={field}
        keyfilter={RecordUtils.getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        onChange={e => onChangeForm(field, e.target.value, isConditional)}
        placeholder={type === 'DATE' ? 'YYYY-MM-DD' : ''}
        ref={inputRef}
        style={{ width: '35%' }}
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
        disabled={column.readOnly && reporting}
        monthNavigator={true}
        onChange={e =>
          onChangeForm(field, RecordUtils.formatDate(e.target.value, isNil(e.target.value)), isConditional)
        }
        inputRef={refCalendar}
        style={{ width: '60px' }}
        value={new Date(RecordUtils.formatDate(fieldValue, isNil(fieldValue)))}
        yearNavigator={true}
        yearRange="2010:2030"
      />
    );
  };

  const renderComplexGeometries = () => {
    return false;
  };

  const renderLinkDropdown = (field, fieldValue) => {
    if (column.pkHasMultipleValues) {
      return (
        <MultiSelect
          addSpaceCommaSeparator={true}
          appendTo={document.body}
          clearButton={false}
          disabled={column.readOnly && reporting}
          filter={true}
          filterPlaceholder={resources.messages['linkFilterPlaceholder']}
          maxSelectedLabels={10}
          onChange={e => onChangeForm(field, e.value, isConditional)}
          onFilterInputChangeBackend={onFilter}
          onFocus={() => {
            if (isEmpty(columnWithLinks.linkItems)) {
              onLoadColsSchema('');
            }
          }}
          options={columnWithLinks.linkItems}
          optionLabel="itemType"
          ref={linkDropdownRef}
          value={RecordUtils.getMultiselectValues(
            columnWithLinks.linkItems,
            !Array.isArray(fieldValue) ? fieldValue.split(', ').join(',') : fieldValue
          )}
        />
      );
    } else {
      return (
        <Dropdown
          appendTo={document.body}
          currentValue={fieldValue}
          disabled={column.readOnly && reporting}
          filter={true}
          filterPlaceholder={resources.messages['linkFilterPlaceholder']}
          filterBy="itemType,value"
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
      hasLegend={true}
      geoJson={fieldValue}
      geometryType={'POINT'}
      onSelectPoint={onSelectPoint}
      selectedCRS={map.currentCRS.value}></Map>
  );

  const renderMapType = (field, fieldValue) => (
    <div>
      <div className={styles.pointEpsgWrapper}>
        <label className={styles.epsg}>{'Coords:'}</label>
        <InputText
          className={`${styles.pointInput} ${map.showCoordinateError && styles.pointInputError}`}
          disabled={column.readOnly && reporting}
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
        {map.showCoordinateError && <span className={styles.pointError}>{resources.messages['wrongCoordinate']}</span>}
      </div>

      <div className={styles.pointEpsgWrapper}>
        <label className={styles.epsg}>{resources.messages['epsg']}</label>
        <Dropdown
          ariaLabel={'crs'}
          appendTo={document.body}
          className={styles.epsgSwitcher}
          disabled={map.isMapDisabled}
          options={crs}
          optionLabel="label"
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
          placeholder="Select a CRS"
          value={map.currentCRS}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          icon="marker"
          onClick={() => onMapOpen(fieldValue)}
          tooltip={resources.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );

  const renderTextarea = (field, fieldValue) => (
    <InputTextarea
      collapsedHeight={75}
      disabled={column.readOnly && reporting}
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
        // disabled={isSaving}
        label={resources.messages['save']}
        icon={'check'}
        onClick={() => onSavePoint(map.newPoint, map.newPointCRS)}
      />
      <Button
        className="p-button-secondary button-right-aligned"
        icon="cancel"
        label={resources.messages['cancel']}
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
    <React.Fragment>
      {renderFieldEditor()}
      {map.isMapOpen && (
        <Dialog
          className={'map-data'}
          blockScroll={false}
          dismissableMask={false}
          footer={saveMapCoordinatesDialogFooter}
          header={resources.messages['geospatialData']}
          modal={true}
          onHide={() => dispatchMap({ type: 'TOGGLE_MAP_VISIBILITY', payload: false })}
          visible={map.isMapOpen}>
          <div className="p-grid p-fluid">{renderMap()}</div>
        </Dialog>
      )}
    </React.Fragment>
  );
};

export { DataFormFieldEditor };
