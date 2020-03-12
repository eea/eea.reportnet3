import React, { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './LinkSelector.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ListBox } from './_components/ListBox';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const LinkSelector = ({ datasetSchemas, isLinkSelectorVisible, onCancelSaveLink, onSaveLink, selectedLink }) => {
  const resources = useContext(ResourcesContext);
  console.log({ selectedLink });
  const [link, setLink] = useState(selectedLink);
  const [isVisible, setIsVisible] = useState(isLinkSelectorVisible);

  const linkSelectorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      {console.log(isUndefined(link), link === '')}
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
      const hasPK = !isUndefined(table.records[0].fields.filter(field => field.isPK === true)[0]);
      if (hasPK) {
        const pkField = table.records[0].fields.filter(field => field.isPK === true)[0];
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
                    console.log(e.value);
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
        {/* <span>{JSON.stringify(referencedField)}</span> */}
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
      {renderLinkSelector()}
    </Dialog>
  );
};

export { LinkSelector };
