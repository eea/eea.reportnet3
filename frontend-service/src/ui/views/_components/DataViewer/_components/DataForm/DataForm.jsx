import React, { useContext, useEffect, useState } from 'react';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputText } from 'ui/views/_components/InputText';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { RecordUtils } from 'ui/views/_functions/Utils';

const DataForm = ({ addDialogVisible, colsSchema, datasetId, editDialogVisible, formType, onChangeForm, records }) => {
  const resources = useContext(ResourcesContext);
  const [colsSchemaWithLinks, setColsSchemaWithLinks] = useState([]);

  useEffect(() => {
    onLoadColsSchema('');
  }, []);

  const onFilter = async filter => {
    onLoadColsSchema(filter);
  };

  const onLoadColsSchema = async filter => {
    const colsSchemasPromises = colsSchema.map(async colSchema => {
      const linkItems = await getLinkItemsWithEmptyOption(filter, colSchema.type, colSchema.referencedField);
      colSchema.linkItems = linkItems;
      return colSchema;
    });

    Promise.all(colsSchemasPromises).then(completedSchemasWithLinks => {
      setColsSchemaWithLinks(completedSchemasWithLinks);
    });
  };

  const getCodelistItemsWithEmptyOption = (colsSchema, field) => {
    const column = colsSchema.filter(e => e.field === field)[0];
    const codelistItems = column.codelistItems.map(codelistItem => {
      return { itemType: codelistItem, value: codelistItem };
    });
    codelistItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return codelistItems;
  };

  const getLinkItems = field => {
    console.log({ field, colsSchema });
    if (!isEmpty(colsSchemaWithLinks)) {
      const column = colsSchemaWithLinks.filter(e => e.field === field)[0];
      return column.linkItems;
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
    const linkItems = referencedFieldValues.map(referencedField => {
      return {
        itemType: referencedField.value,
        value: referencedField.value
      };
    });
    linkItems.unshift({
      itemType: resources.messages['noneCodelist'],
      value: ''
    });
    return linkItems;
  };

  const renderDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        onChange={e => {
          onChangeForm(field, e.target.value.value);
        }}
        optionLabel="itemType"
        options={getCodelistItemsWithEmptyOption(colsSchema, field)}
        value={RecordUtils.getCodelistValue(RecordUtils.getCodelistItems(colsSchema, field), fieldValue)}
      />
    );
  };

  const renderLinkDropdown = (field, fieldValue) => {
    return (
      <Dropdown
        appendTo={document.body}
        filter={true}
        filterPlaceholder={resources.messages['linkFilterPlaceholder']}
        filterBy="itemType,value"
        onChange={e => {
          onChangeForm(field, e.target.value.value);
        }}
        onFilterInputChangeBackend={onFilter}
        optionLabel="itemType"
        options={getLinkItems(field)}
        value={RecordUtils.getLinkValue(getLinkItems(field), fieldValue)}
      />
    );
  };

  const editRecordForm = colsSchema.map((column, i) => {
    //Avoid row id Field and dataSetPartitionId
    if (editDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.editedRecord.dataRow)) {
          const field = records.editedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                {column.type === 'CODELIST' ? (
                  renderDropdown(
                    column.field,
                    isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]
                  )
                ) : column.type === 'LINK' ? (
                  renderLinkDropdown(
                    column.field,
                    isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]
                  )
                ) : (
                  <InputText
                    id={column.field}
                    onChange={e => onChangeForm(column.field, e.target.value)}
                    value={isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]}
                  />
                )}
              </div>
            </React.Fragment>
          );
        }
      }
    }
  });

  const newRecordForm = colsSchema.map((column, i) => {
    if (addDialogVisible) {
      if (i < colsSchema.length - 2) {
        if (!isUndefined(records.newRecord.dataRow)) {
          const field = records.newRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
          return (
            <React.Fragment key={column.field}>
              <div className="p-col-4" style={{ padding: '.75em' }}>
                <label htmlFor={column.field}>{column.header}</label>
              </div>
              <div className="p-col-8" style={{ padding: '.5em' }}>
                {column.type === 'CODELIST' ? (
                  renderDropdown(
                    column.field,
                    isNull(field.fieldData[column.field]) || isUndefined(field.fieldData[column.field])
                      ? ''
                      : field.fieldData[column.field]
                  )
                ) : column.type === 'LINK' ? (
                  renderLinkDropdown(
                    column.field,
                    isNil(field.fieldData[column.field]) ? '' : field.fieldData[column.field]
                  )
                ) : (
                  <InputText id={column.field} onChange={e => onChangeForm(column.field, e.target.value, field)} />
                )}
              </div>
            </React.Fragment>
          );
        }
      }
    }
  });

  return formType === 'EDIT' ? editRecordForm : newRecordForm;
};

export { DataForm };
