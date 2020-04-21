import React, { useContext, useEffect, useState } from 'react';

import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const DataFormFieldEditor = ({ column, datasetId, field, fieldValue = '', onChangeForm, type }) => {
  const resources = useContext(ResourcesContext);
  const [columnWithLinks, setColumnWithLinks] = useState([]);

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

  const getFilter = type => {
    switch (type) {
      case 'NUMBER':
      case 'POINT':
      case 'COORDINATE_LONG':
      case 'COORDINATE_LAT':
        return 'num';
      case 'DATE':
        return 'date';
      case 'TEXT':
        return 'any';
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
    const codelistItems = column.codelistItems.map(codelistItem => {
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
      options={getCodelistItemsWithEmptyOption(field)}
      value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItemsInSingleColumn(column), fieldValue)}
    />
  );

  const renderFieldEditor = () =>
    type === 'CODELIST' ? (
      renderCodelistDropdown(field, fieldValue)
    ) : type === 'LINK' ? (
      renderLinkDropdown(field, fieldValue)
    ) : (
      <InputText
        id={field}
        keyfilter={getFilter(type)}
        onChange={e => onChangeForm(field, e.target.value)}
        value={fieldValue}
        // type={type === 'DATE' ? 'date' : 'text'}
        placeHolder={type === 'DATE' ? 'YYYY-MM-DD' : ''}
        type="text"
      />
    );

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

  return <React.Fragment>{renderFieldEditor()}</React.Fragment>;
};

export { DataFormFieldEditor };
