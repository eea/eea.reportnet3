import React, { useContext, useEffect, useRef, useState } from 'react';

// import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

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
  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [columnWithLinks, setColumnWithLinks] = useState([]);
  // const [isAttachFileVisible, setIsAttachFileVisible] = useState(false);
  const [isDeleteAttachmentVisible, setIsDeleteAttachmentVisible] = useState(false);
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [mapCoordinates, setMapCoordinates] = useState();

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

  const onSavePoint = coordinates => {
    setIsMapOpen(false);
    onChangeForm(field, coordinates.join(', '));

    // onEditorSubmitValue(cells, coordinates.join(', '));
  };

  const onSelectPoint = coordinates => setMapCoordinates(coordinates);

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
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
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

  const onConfirmDeleteAttachment = () => {
    onChangeForm(field, []);
    setIsDeleteAttachmentVisible(false);
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
    ) : (
      <InputText
        disabled={column.readOnly && reporting}
        id={field}
        keyfilter={getFilter(type)}
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
        onChange={e => onChangeForm(field, formatDate(e.target.value, isNil(e.target.value)))}
        appendTo={document.body}
        baseZIndex={9999}
        dateFormat="yy-mm-dd"
        disabled={column.readOnly && reporting}
        monthNavigator={true}
        style={{ width: '60px' }}
        value={new Date(formatDate(fieldValue, isNil(fieldValue)))}
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

  const renderMap = () => <Map coordinates={mapCoordinates} onSelectPoint={onSelectPoint} selectButton={true}></Map>;

  const renderMapType = (field, fieldValue) => (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <InputText
        disabled={column.readOnly && reporting}
        keyfilter={getFilter(type)}
        // onBlur={e => onEditorSubmitValue(cells, e.target.value, record)}
        onChange={e => onChangeForm(field, e.target.value)}
        // onFocus={e => {
        //   e.preventDefault();
        //   onEditorValueFocus(cells, e.target.value);
        // }}
        // onKeyDown={e => onEditorKeyChange(cells, e, record)}
        style={{ width: '35%' }}
        type="text"
        value={fieldValue}
      />
      <Button
        className={`p-button-secondary-transparent button`}
        icon="marker"
        onClick={() => onMapOpen(fieldValue)}
        // style={{ marginLeft: '0.4rem', alignSelf: !fieldDesignerState.isEditing ? 'center' : 'baseline' }}
        style={{ width: '2.357em', marginLeft: '0.5rem' }}
        tooltip={resources.messages['selectGeographicalDataOnMap']}
        tooltipOptions={{ position: 'bottom' }}
      />
    </div>
  );

  const saveMapCoordinatesDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-animated-blink"
        // disabled={isSaving}
        label={resources.messages['save']}
        icon={'check'}
        onClick={() => onSavePoint(mapCoordinates)}
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
          dismissableMask={false}
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
