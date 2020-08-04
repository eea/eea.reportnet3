import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './LinkSelector.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const LinkSelector = withRouter(
  ({
    hasMultipleValues = false,
    isLinkSelectorVisible,
    match,
    mustBeUsed = false,
    onCancelSaveLink,
    onSaveLink,
    selectedLink,
    tableSchemaId
  }) => {
    const resources = useContext(ResourcesContext);
    const [datasetSchemas, setDatasetSchemas] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isVisible, setIsVisible] = useState(isLinkSelectorVisible);
    const [link, setLink] = useState(selectedLink);
    const [pkHasMultipleValues, setPkHasMultipleValues] = useState(hasMultipleValues);
    const [pkMustBeUsed, setPkMustBeUsed] = useState(mustBeUsed);

    const {
      params: { dataflowId }
    } = match;

    useEffect(() => {
      const getDatasetSchemas = async () => {
        setIsLoading(true);
        const datasetSchemasDTO = await DataflowService.getAllSchemas(dataflowId);
        setIsLoading(false);
        setDatasetSchemas(datasetSchemasDTO);
      };

      getDatasetSchemas();
    }, []);

    const linkSelectorDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          disabled={isUndefined(link) || isEmpty(link)}
          icon="check"
          label={resources.messages['save']}
          onClick={() => {
            onSaveLink(link, pkMustBeUsed, pkHasMultipleValues);
            setIsVisible(false);
          }}
        />
        <Button
          className="p-button-secondary"
          icon="cancel"
          label={resources.messages['cancel']}
          onClick={() => {
            onCancelSaveLink(link, pkMustBeUsed, pkHasMultipleValues);
            setIsVisible(false);
          }}
        />
      </div>
    );

    const getOptions = datasetSchema =>
      datasetSchema.tables.map(table => {
        const hasPK = !isUndefined(table.records[0].fields.filter(field => field.pk === true)[0]);
        if (hasPK && table.tableSchemaId !== tableSchemaId) {
          const pkField = table.records[0].fields.filter(field => field.pk === true)[0];
          return {
            name: `${table.tableSchemaName} - ${pkField.name}`,
            value: `${table.tableSchemaName} - ${pkField.fieldId}`,
            referencedField: { fieldSchemaId: pkField.fieldId, datasetSchemaId: datasetSchema.datasetSchemaId },
            disabled: false
          };
        } else if (table.tableSchemaId === tableSchemaId) {
          return {
            name: `${table.tableSchemaName} - ${resources.messages['noSelectablePK']}`,
            value: `${table.tableSchemaName} - ${resources.messages['noSelectablePK']}`,
            referencedField: null,
            disabled: true
          };
        } else {
          return {
            name: `${table.tableSchemaName} - ${resources.messages['noPK']}`,
            value: `${table.tableSchemaName} - ${resources.messages['noPK']}`,
            referencedField: null,
            disabled: true
          };
        }
      });

    const renderLinkSelector = () => {
      return (
        <React.Fragment>
          <div className={styles.schemaWrapper}>
            {!isUndefined(datasetSchemas) &&
              !isEmpty(datasetSchemas) &&
              datasetSchemas.map(datasetSchema => {
                return (
                  <ListBox
                    options={getOptions(datasetSchema)}
                    onChange={e => {
                      if (!isNil(e.value)) {
                        setLink(e.value);
                      }
                    }}
                    optionLabel="name"
                    optionValue="value"
                    title={datasetSchema.datasetSchemaName}
                    value={link}></ListBox>
                );
              })}
          </div>
          <div className={styles.selectedLinkWrapper}>
            <span className={styles.switchTextInput}>{resources.messages['pkValuesMustBeUsed']}</span>
            <Checkbox
              checked={pkMustBeUsed}
              id={'pkMustBeUsed_check'}
              inputId={'pkMustBeUsed_check'}
              label="Default"
              onChange={e => setPkMustBeUsed(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem' }}
            />
            <label htmlFor={'pkMustBeUsed_check'} className="srOnly">
              {resources.messages['pkValuesMustBeUsed']}
            </label>
            <span className={styles.switchTextInput}>{resources.messages['pkHasMultipleValues']}</span>
            <Checkbox
              checked={pkHasMultipleValues}
              id={'pkHasMultipleValues_check'}
              inputId={'pkHasMultipleValues_check'}
              label="Default"
              onChange={e => setPkHasMultipleValues(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem' }}
            />
            <label htmlFor={'pkHasMultipleValues_check'} className="srOnly">
              {resources.messages['pkHasMultipleValues']}
            </label>
          </div>
          <div className={styles.selectedLinkWrapper}>
            <span>{`${resources.messages['selectedLink']}: `}</span>
            <span>{!isNil(link) ? link.name : ''}</span>
          </div>
        </React.Fragment>
      );
    };

    return (
      <Dialog
        blockScroll={false}
        contentStyle={{ overflow: 'auto' }}
        closeOnEscape={false}
        footer={linkSelectorDialogFooter}
        header={resources.messages['linkSelector']}
        modal={true}
        onHide={() => {
          onCancelSaveLink(link, pkMustBeUsed, pkHasMultipleValues);
          setIsVisible(false);
        }}
        style={{ minWidth: '55%' }}
        visible={isVisible}
        zIndex={3003}>
        {isLoading ? <Spinner className={styles.positioning} /> : renderLinkSelector()}
      </Dialog>
    );
  }
);

export { LinkSelector };
