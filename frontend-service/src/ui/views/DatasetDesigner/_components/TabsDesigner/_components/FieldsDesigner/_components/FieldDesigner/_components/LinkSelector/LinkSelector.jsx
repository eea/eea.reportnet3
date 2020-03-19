import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './LinkSelector.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from './_components/ListBox';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const LinkSelector = withRouter(({ isLinkSelectorVisible, match, onCancelSaveLink, onSaveLink, selectedLink }) => {
  const resources = useContext(ResourcesContext);
  const [datasetSchemas, setDatasetSchemas] = useState([]);
  const [isVisible, setIsVisible] = useState(isLinkSelectorVisible);
  const [link, setLink] = useState(selectedLink);
  const [isLoading, setIsLoading] = useState(false);

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
          onSaveLink(link);
          setIsVisible(false);
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onCancelSaveLink();
          setIsVisible(false);
        }}
      />
    </div>
  );

  const getOptions = datasetSchema =>
    datasetSchema.tables.map(table => {
      const hasPK = !isUndefined(table.records[0].fields.filter(field => field.pk === true)[0]);
      if (hasPK) {
        const pkField = table.records[0].fields.filter(field => field.pk === true)[0];
        return {
          name: `${table.tableSchemaName} - ${pkField.name}`,
          value: `${table.tableSchemaName} - ${pkField.fieldId}`,
          referencedField: { fieldSchemaId: pkField.fieldId, datasetSchemaId: datasetSchema.datasetSchemaId },
          disabled: false
        };
      } else {
        return {
          name: `${table.tableSchemaName} - No PK`,
          value: `${table.tableSchemaName} - No PK`,
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
      onHide={() => setIsVisible(false)}
      style={{ width: '80%' }}
      visible={isVisible}
      zIndex={3003}>
      {isLoading ? <Spinner className={styles.positioning} /> : renderLinkSelector()}
    </Dialog>
  );
});

export { LinkSelector };
