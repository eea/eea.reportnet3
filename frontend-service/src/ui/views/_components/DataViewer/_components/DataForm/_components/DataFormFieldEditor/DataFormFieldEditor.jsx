import React, { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';
import { Map } from 'ui/views/_components/Map';
import { MultiSelect } from 'ui/views/_components/MultiSelect';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const DataFormFieldEditor = ({ column, datasetId, field, fieldValue = '', onChangeForm, type }) => {
  const resources = useContext(ResourcesContext);
  const [columnWithLinks, setColumnWithLinks] = useState([]);
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [mapCoordinates, setMapCoordinates] = useState();

  useEffect(() => {
    if (!isUndefined(fieldValue)) {
      if (type === 'LINK') onLoadColsSchema(fieldValue);
    }
  }, []);

  const onFilter = async filter => {
    onLoadColsSchema(filter);
  };

  const onLoadColsSchema = async filter => {
    const inmColumn = { ...column };
    const linkItems = await getLinkItemsWithEmptyOption(filter, type, column.referencedField);
    inmColumn.linkItems = linkItems;
    setColumnWithLinks(inmColumn);
  };

  const onMapOpen = coordinates => {
    setIsMapOpen(true);
    setMapCoordinates(coordinates);
  };

  const onSelectPoint = coordinates => {
    setIsMapOpen(false);
    onChangeForm(field, coordinates.join(', '));

    // onEditorSubmitValue(cells, coordinates.join(', '));
  };

  const formatDate = date => {
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
        // case 'RICH_TEXT':
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

  const getLinkItemsWithEmptyOption = async (filter, type, referencedField) => {
    if (isNil(type) || type.toUpperCase() !== 'LINK' || isNil(referencedField)) {
      return [];
    }
    const referencedFieldValues = await DatasetService.getReferencedFieldValues(
      datasetId,
      isUndefined(referencedField.name) ? referencedField.idPk : referencedField.referencedField.fieldSchemaId,
      filter
    );
    const linkItems = referencedFieldValues
      .map(referencedField => {
        return {
          itemType: referencedField.value,
          value: referencedField.value
        };
      })
      .sort((a, b) => a.value - b.value);
    linkItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
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

  const renderCodelistDropdown = (field, fieldValue) => (
    <Dropdown
      appendTo={document.body}
      onChange={e => {
        onChangeForm(field, e.target.value.value);
      }}
      optionLabel="itemType"
      options={getCodelistItemsWithEmptyOption()}
      value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
    />
  );

  const renderMultiselectCodelist = (field, fieldValue) => {
    return (
      <MultiSelect
        appendTo={document.body}
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
      // case 'RICH_TEXT':
      //   return richTextCharacters;
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
    ) : (
      <InputText
        id={field}
        keyfilter={getFilter(type)}
        maxLength={getMaxCharactersByType(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        // type={type === 'DATE' ? 'date' : 'text'}
        placeholder={type === 'DATE' ? 'YYYY-MM-DD' : ''}
        style={{ width: '35%' }}
        type="text"
        value={fieldValue}
      />
    );

  const renderCalendar = (field, fieldValue) => {
    return (
      <Calendar
        onChange={e => onChangeForm(field, formatDate(e.target.value))}
        appendTo={document.body}
        baseZIndex={9999}
        dateFormat="yy-mm-dd"
        monthNavigator={true}
        style={{ width: '60px' }}
        value={new Date(formatDate(fieldValue))}
        yearNavigator={true}
        yearRange="2010:2030"
      />
    );
  };

  const renderLinkDropdown = (field, fieldValue) => (
    <Dropdown
      appendTo={document.body}
      currentValue={fieldValue}
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

  const renderMap = () => <Map coordinates={mapCoordinates} onSelectPoint={onSelectPoint} selectButton={true}></Map>;

  const renderMapType = (field, fieldValue) => (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <InputText
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

  return (
    <React.Fragment>
      {renderFieldEditor()}
      {isMapOpen && (
        <Dialog
          className={'map-data'}
          blockScroll={false}
          dismissableMask={false}
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
