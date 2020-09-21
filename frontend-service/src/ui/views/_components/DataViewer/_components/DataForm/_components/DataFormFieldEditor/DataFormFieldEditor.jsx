import React, { useContext, useEffect, useRef, useState } from 'react';

// import isEmpty from 'lodash/isEmpty';
import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import proj4 from 'proj4';

import styles from './DataFormFieldEditor.module.scss';

// import { DatasetConfig } from 'conf/domain/model/Dataset';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
// import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
// import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { Map } from 'ui/views/_components/Map';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

// import { getUrl } from 'core/infrastructure/CoreUtils';
import { MapUtils } from 'ui/views/_functions/Utils/MapUtils';
import { RecordUtils } from 'ui/views/_functions/Utils';

const DataFormFieldEditor = ({
  autoFocus,
  column,
  datasetId,
  field,
  fieldValue = '',
  hasWritePermissions,
  isVisible,
  onChangeForm,
  reporting,
  type
}) => {
  const crs = [
    { label: 'WGS84', value: 'EPSG:4326' },
    { label: 'ETRS89', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG:3035' }
  ];

  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [columnWithLinks, setColumnWithLinks] = useState([]);

  const [currentCRS, setCurrentCRS] = useState(
    fieldValue !== '' && type === 'POINT'
      ? crs.filter(crsItem => crsItem.value === JSON.parse(fieldValue).properties.rsid)[0]
      : { label: 'WGS84', value: 'EPSG:4326' }
  );
  // const [isAttachFileVisible, setIsAttachFileVisible] = useState(false);
  // const [isDeleteAttachmentVisible, setIsDeleteAttachmentVisible] = useState(false);
  const [isMapDisabled, setIsMapDisabled] = useState(false);
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [mapCoordinates, setMapCoordinates] = useState('');
  const [newPoint, setNewPoint] = useState('');
  const [newPointCRS, setNewPointCRS] = useState('EPSG:4326');

  useEffect(() => {
    if (!isUndefined(fieldValue)) {
      if (type === 'LINK') onLoadColsSchema(fieldValue);
    }
  }, []);

  useEffect(() => {
    if (inputRef.current && isVisible && autoFocus) {
      inputRef.current.element.focus();
    }
  }, [inputRef.current, isVisible]);

  const onFilter = async filter => {
    onLoadColsSchema(filter);
  };

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
    setIsMapOpen(true);
    setMapCoordinates(coordinates);
  };

  const onSavePoint = (coordinates, crs) => {
    console.log(MapUtils.parseCoordinates(coordinates), coordinates, crs);
    if (coordinates !== '') {
      const inmMapGeoJson = cloneDeep(fieldValue);
      const parsedInmMapGeoJson = JSON.parse(inmMapGeoJson);
      parsedInmMapGeoJson.geometry.coordinates = MapUtils.parseCoordinates(coordinates);
      parsedInmMapGeoJson.properties.rsid = crs.value;
      onChangeForm(field, JSON.stringify(parsedInmMapGeoJson));
    }
    setCurrentCRS(crs);
    setIsMapOpen(false);
  };

  const onSelectPoint = (coordinates, selectedCrs) => {
    setMapCoordinates(coordinates);
    setNewPoint(coordinates);
    console.log({ coordinates });
    const filteredCrs = crs.filter(crsItem => crsItem.value === selectedCrs)[0];
    // setCurrentCRS(filteredCrs);
    setNewPointCRS(filteredCrs);
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
      //withCRS ? `${projectedCoordinates.join(', ')}, ${crs.value}` : `${projectedCoordinates.join(', ')}`;
    }
  };

  const getLinkItemsWithEmptyOption = async (filter, type, referencedField, hasMultipleValues) => {
    if (isNil(type) || type.toUpperCase() !== 'LINK' || isNil(referencedField)) {
      return [];
    }
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      isUndefined(referencedField.name) ? referencedField.idPk : referencedField.referencedField.fieldSchemaId,
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

    // const hasMultipleValues = RecordUtils.getCellInfo(colsSchema, cells.field).pkHasMultipleValues;
    if (!hasMultipleValues) {
      linkItems.unshift({
        itemType: resources.messages['noneCodelist'],
        value: ''
      });
    }
    return linkItems;
  };

  const getCodelistItemsWithEmptyOption = () => {
    const codelistItems = column.codelistItems.sort().map(codelistItem => {
      return { itemType: codelistItem, value: codelistItem };
    });

    codelistItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistItems;
  };

  const projectCoordinates = (coordinates, newCRS) => {
    return proj4(proj4(currentCRS.value), proj4(newCRS), coordinates);
  };

  const renderCodelistDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        disabled={column.readOnly && reporting}
        onChange={e => {
          onChangeForm(field, e.target.value.value);
        }}
        optionLabel="itemType"
        options={getCodelistItemsWithEmptyOption()}
        value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
      />
    );
  };

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        appendTo={document.body}
        disabled={column.readOnly && reporting}
        maxSelectedLabels={10}
        onChange={e => onChangeForm(field, e.value)}
        options={column.codelistItems.sort().map(codelistItem => {
          return { itemType: codelistItem, value: codelistItem };
        })}
        optionLabel="itemType"
        style={{ height: '34px' }}
        value={RecordUtils.getMultiselectValues(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
        // hasSelectedItemsLabel={false}
      />
    );
  };

  // const getAttachExtensions = [{ fileExtension: '.csv, .txt, .pdf' }].map(file => `.${file.fileExtension}`).join(', ');

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

  // const onConfirmDeleteAttachment = () => {
  //   onChangeForm(field, []);
  //   setIsDeleteAttachmentVisible(false);
  // };

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
    ) : (
      <InputText
        disabled={column.readOnly && reporting}
        id={field}
        keyfilter={RecordUtils.getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        // type={type === 'DATE' ? 'date' : 'text'}
        placeholder={type === 'DATE' ? 'YYYY-MM-DD' : ''}
        ref={inputRef}
        style={{ width: '35%' }}
        type="text"
        value={fieldValue}
      />
    );

  const renderAttachment = (field, fieldValue = []) => {
    return false;
    // console.log({ field, fieldValue }, fieldValue.split('|'));
    // const splittedFieldValue = fieldValue.split('|');
    // return (
    //   <div style={{ display: 'flex' }}>
    //     {!isEmpty(fieldValue) && (
    //       <Button
    //         className={`${isEmpty(splittedFieldValue[0]) && 'p-button-animated-blink'} p-button-secondary-transparent`}
    //         icon="export"
    //         iconPos="right"
    //         label={splittedFieldValue[0]}
    //         onClick={() => {
    //           console.log('Download');
    //           const a = document.createElement('a');
    //           a.href = `data:text/plain;base64,${splittedFieldValue[2]}`;
    //           a.download = splittedFieldValue[0];
    //           a.click();
    //         }}
    //         style={{ width: 'fit-content' }}
    //       />
    //     )}

    //     <Button
    //       className={`p-button-animated-blink p-button-secondary-transparent`}
    //       icon="import"
    //       onClick={() => {
    //         setIsAttachFileVisible(true);
    //       }}
    //     />
    //     {!isEmpty(fieldValue) && (
    //       <Button
    //         className={`p-button-animated-blink p-button-secondary-transparent`}
    //         icon="trash"
    //         onClick={() => setIsDeleteAttachmentVisible(true)}
    //       />
    //     )}
    //   </div>
    // );
  };

  const renderCalendar = (field, fieldValue) => {
    return (
      <Calendar
        onChange={e => onChangeForm(field, RecordUtils.formatDate(e.target.value, isNil(e.target.value)))}
        appendTo={document.body}
        baseZIndex={9999}
        dateFormat="yy-mm-dd"
        disabled={column.readOnly && reporting}
        monthNavigator={true}
        style={{ width: '60px' }}
        value={new Date(RecordUtils.formatDate(fieldValue, isNil(fieldValue)))}
        yearNavigator={true}
        yearRange="2010:2030"
      />
    );
  };

  // const renderCustomFileAttachFooter = (
  //   <Button
  //     className="p-button-secondary p-button-animated-blink"
  //     icon={'cancel'}
  //     label={resources.messages['close']}
  //     onClick={() => setIsAttachFileVisible(false)}
  //   />
  // );

  const renderLinkDropdown = (field, fieldValue) => {
    if (column.pkHasMultipleValues) {
      return (
        <MultiSelect
          appendTo={document.body}
          clearButton={false}
          disabled={column.readOnly && reporting}
          filter={true}
          filterPlaceholder={resources.messages['linkFilterPlaceholder']}
          maxSelectedLabels={10}
          onChange={e => onChangeForm(field, e.value)}
          onFilterInputChangeBackend={onFilter}
          options={columnWithLinks.linkItems}
          optionLabel="itemType"
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
            onChangeForm(field, e.target.value.value);
          }}
          onFilterInputChangeBackend={onFilter}
          optionLabel="itemType"
          options={columnWithLinks.linkItems}
          showFilterClear={true}
          value={RecordUtils.getLinkValue(columnWithLinks.linkItems, fieldValue)}
        />
      );
    }
  };

  const renderMap = () => (
    <Map
      geoJson={
        fieldValue !== ''
          ? fieldValue
          : `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`
      }
      onSelectPoint={onSelectPoint}
      selectedCRS={currentCRS.value}></Map>
  );

  const renderMapType = (field, fieldValue) => (
    <div>
      <div className={styles.pointSridWrapper}>
        <label className={styles.srid}>{'Coords:'}</label>
        <InputText
          disabled={column.readOnly && reporting}
          keyfilter={RecordUtils.getFilter(type)}
          onBlur={e =>
            onChangeForm(field, changePoint(JSON.parse(fieldValue), e.target.value, currentCRS.value, false))
          }
          onChange={e => {
            if (fieldValue === '') {
              fieldValue = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`;
            }
            onChangeForm(field, changePoint(JSON.parse(fieldValue), e.target.value, currentCRS.value, false, false));
          }}
          // onFocus={e => {
          //   e.preventDefault();
          //   onEditorValueFocus(cells, e.target.value);
          // }}
          // onKeyDown={e => onEditorKeyChange(cells, e, record)}
          style={{ width: '50%' }}
          type="text"
          value={
            fieldValue !== ''
              ? JSON.parse(fieldValue).geometry.coordinates.join(', ')
              : JSON.parse(
                  `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`
                ).geometry.coordinates.join(', ')
          }
        />
      </div>
      {console.log({ currentCRS })}
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
            onChangeForm(
              field,
              changePoint(JSON.parse(fieldValue), JSON.parse(fieldValue).geometry.coordinates, e.target.value)
            );
            setCurrentCRS(e.target.value);
            // onChangePointCRS(e.target.value.value);
          }}
          placeholder="Select a CRS"
          style={{ width: '50%', minWidth: '50%' }}
          value={currentCRS}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          disabled={isMapDisabled}
          icon="marker"
          onClick={() => onMapOpen(fieldValue)}
          tooltip={resources.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );

  const saveMapCoordinatesDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-animated-blink"
        // disabled={isSaving}
        label={resources.messages['save']}
        icon={'check'}
        onClick={() => onSavePoint(newPoint, newPointCRS)}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          // dispatchRecords({
          //   type: 'SET_NEW_RECORD',
          //   payload: RecordUtils.createEmptyObject(colsSchema, undefined)
          // });
          setNewPoint('');
          setNewPointCRS(currentCRS.value);
          setIsMapOpen(false);
        }}
      />
    </div>
  );

  return (
    <React.Fragment>
      {renderFieldEditor()}
      {/* {isAttachFileVisible && (
        <Dialog
          // className={styles.Dialog}
          footer={renderCustomFileAttachFooter}
          header={`${resources.messages['uploadAttachment']}`}
          onHide={() => setIsAttachFileVisible(false)}
          visible={isAttachFileVisible}>
          <CustomFileUpload
            accept={getAttachExtensions}
            // accept=".*"
            chooseLabel={resources.messages['selectFile']}
            // className={styles.FileUpload}
            fileLimit={1}
            infoTooltip={infoAttachTooltip}
            mode="advanced"
            multiple={false}
            invalidExtensionMessage={resources.messages['invalidExtensionFile']}
            name="file"
            onUpload={e => onAttach(e)}
            url={`${window.env.REACT_APP_BACKEND}${getUrl(DatasetConfig.importTableData, {
              datasetId: datasetId
            })}`}
          />
        </Dialog>
      )} */}
      {/* {isDeleteAttachmentVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={`${resources.messages['deleteAttachmentHeader']}`}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onConfirmDeleteAttachment}
          onHide={() => setIsDeleteAttachmentVisible(false)}
          visible={isDeleteAttachmentVisible}>
          {resources.messages['deleteAttachmentConfirm']}
        </ConfirmDialog>
      )} */}

      {isMapOpen && (
        <Dialog
          className={'map-data'}
          blockScroll={false}
          dismissableMask={false}
          footer={saveMapCoordinatesDialogFooter}
          header={resources.messages['geospatialData']}
          modal={true}
          onHide={() => setIsMapOpen(false)}
          // style={{ height: '90vh', width: '80%' }}
          visible={isMapOpen}>
          <div className="p-grid p-fluid">{renderMap()}</div>
        </Dialog>
      )}
    </React.Fragment>
  );
};

export { DataFormFieldEditor };
