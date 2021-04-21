import { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import pick from 'lodash/pick';

import styles from './LinkSelector.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'primereact/checkbox';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { ListBox } from 'ui/views/DatasetDesigner/_components/ListBox';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { linkSelectorReducer } from './_functions/Reducers/linkSelectorReducer';

const LinkSelector = withRouter(
  ({
    datasetSchemaId,
    hasMultipleValues = false,
    isLinkSelectorVisible,
    linkedTableConditional,
    linkedTableLabel,
    masterTableConditional,
    match,
    mustBeUsed = false,
    onCancelSaveLink,
    onSaveLink,
    selectedLink,
    tableSchemaId
  }) => {
    const resources = useContext(ResourcesContext);
    const [linkSelectorState, dispatchLinkSelector] = useReducer(linkSelectorReducer, {
      link: {
        ...selectedLink,
        referencedField: !isNil(selectedLink)
          ? pick(selectedLink.referencedField, 'datasetSchemaId', 'fieldSchemaId', 'tableSchemaId')
          : null
      },
      linkedTableFields: [],
      pkLinkedTableLabel: {},
      pkLinkedTableConditional: {},
      masterTableConditional: {},
      masterTableFields: []
    });

    const {
      link,
      linkedTableFields,
      pkLinkedTableLabel,
      pkLinkedTableConditional,
      pkMasterTableConditional,
      masterTableFields
    } = linkSelectorState;

    const [datasetSchemas, setDatasetSchemas] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isVisible, setIsVisible] = useState(isLinkSelectorVisible);
    const [pkHasMultipleValues, setPkHasMultipleValues] = useState(hasMultipleValues);
    const [pkMustBeUsed, setPkMustBeUsed] = useState(mustBeUsed);

    const {
      params: { dataflowId }
    } = match;

    const [isSaved, setIsSaved] = useState(false);

    useEffect(() => {
      const getDatasetSchemas = async () => {
        setIsLoading(true);
        const datasetSchemasDTO = await DataflowService.getAllSchemas(dataflowId);
        setIsLoading(false);
        setDatasetSchemas(datasetSchemasDTO.data);
      };

      getDatasetSchemas();
    }, []);

    useEffect(() => {
      if (!isEmpty(datasetSchemas) && !isNil(selectedLink)) {
        getFields(selectedLink);
      }
    }, [datasetSchemas]);

    useEffect(() => {
      if (!isEmpty(linkedTableFields)) {
        dispatchLinkSelector({
          type: 'SET_LINKED_TABLE_FIELDS',
          payload: {
            label: linkedTableFields.find(linkedField => linkedField.fieldSchemaId === linkedTableLabel),
            conditional: linkedTableFields.find(linkedField => linkedField.fieldSchemaId === linkedTableConditional)
          }
        });
      }
    }, [linkedTableFields]);

    useEffect(() => {
      if (!isEmpty(masterTableFields)) {
        dispatchLinkSelector({
          type: 'SET_MASTER_TABLE_CONDITIONAL',
          payload: masterTableFields.find(linkedField => linkedField.fieldSchemaId === masterTableConditional)
        });
      }
    }, [masterTableFields]);

    useEffect(() => {
      if (isSaved) {
        setIsVisible(false);
      }
    }, [isSaved]);

    useEffect(() => {
      if (!isVisible && isSaved) {
        onSaveLink({
          link,
          linkedTableConditional: !isNil(pkLinkedTableConditional) ? pkLinkedTableConditional.fieldSchemaId : '',
          linkedTableLabel: !isNil(pkLinkedTableLabel) ? pkLinkedTableLabel.fieldSchemaId : '',
          masterTableConditional: !isNil(pkMasterTableConditional) ? pkMasterTableConditional.fieldSchemaId : '',
          pkHasMultipleValues,
          pkMustBeUsed
        });
      }
    }, [isVisible]);

    const linkSelectorDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          disabled={isUndefined(link) || isEmpty(link) || (!isNil(link) && isNil(link.referencedField))}
          icon="check"
          label={resources.messages['save']}
          onClick={() => {
            setIsSaved(true);
          }}
        />
        <Button
          className="p-button-secondary button-right-aligned"
          icon="cancel"
          label={resources.messages['cancel']}
          onClick={() => {
            if (!isNil(link) && !isNil(link.referencedField)) {
              onCancelSaveLink({
                link,
                linkedTableConditional: !isNil(pkLinkedTableConditional) ? pkLinkedTableConditional.fieldSchemaId : '',
                linkedTableLabel: !isNil(pkLinkedTableLabel) ? pkLinkedTableLabel.fieldSchemaId : '',
                masterTableConditional: !isNil(pkMasterTableConditional) ? pkMasterTableConditional.fieldSchemaId : '',
                pkHasMultipleValues,
                pkMustBeUsed
              });
            }
            setIsVisible(false);
          }}
        />
      </div>
    );

    const getFields = field => {
      let linkedFields = [];
      let masterFields = [];
      const linkedTable = datasetSchemas
        .find(datasetSchema => datasetSchema.datasetSchemaId === field.referencedField.datasetSchemaId)
        .tables.find(table => table.tableSchemaId === field.referencedField.tableSchemaId);

      linkedFields = linkedTable?.records[0]?.fields
        .filter(
          field =>
            !field.pk &&
            !['ATTACHMENT', 'POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
              field.type.toUpperCase()
            )
        )
        .map(field => {
          return { fieldSchemaId: field.fieldId, name: field.name };
        });
      linkedFields?.unshift({
        name: resources.messages['noneCodelist'],
        fieldSchemaId: ''
      });

      const masterTable = datasetSchemas
        .find(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaId)
        .tables.find(table => table.tableSchemaId === tableSchemaId);
      masterFields = masterTable?.records[0].fields
        .filter(
          field =>
            !field.pk &&
            !['ATTACHMENT', 'POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
              field.type.toUpperCase()
            )
        )
        .map(field => {
          return { fieldSchemaId: field.fieldId, name: field.name };
        });

      masterFields?.unshift({
        name: resources.messages['noneCodelist'],
        fieldSchemaId: ''
      });

      dispatchLinkSelector({ type: 'SET_LINKED_AND_MASTER_FIELDS', payload: { linkedFields, masterFields } });
    };

    const getOptions = datasetSchema =>
      datasetSchema.tables.map(table => {
        const hasPK = !isUndefined(table.records[0].fields.filter(field => field.pk === true)[0]);
        if (hasPK && table.tableSchemaId !== tableSchemaId) {
          const pkField = table.records[0].fields.filter(field => field.pk === true)[0];
          if (
            !['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(pkField.type)
          ) {
            return {
              name: `${table.tableSchemaName} - ${pkField.name}`,
              value: `${table.tableSchemaName} - ${pkField.fieldId}`,
              referencedField: {
                fieldSchemaId: pkField.fieldId,
                datasetSchemaId: datasetSchema.datasetSchemaId,
                tableSchemaId: table.tableSchemaId
              },
              disabled: false
            };
          } else {
            return {
              name: `${table.tableSchemaName} - ${resources.messages['noSelectablePK']}`,
              value: `${table.tableSchemaName} - ${resources.messages['noSelectablePK']}`,
              referencedField: null,
              disabled: true
            };
          }
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
        <Fragment>
          <div className={styles.schemaWrapper}>
            {!isUndefined(datasetSchemas) &&
              !isEmpty(datasetSchemas) &&
              datasetSchemas.map((datasetSchema, i) => {
                return (
                  <ListBox
                    key={`datasetSchema_${i}`}
                    options={getOptions(datasetSchema)}
                    onChange={e => {
                      if (!isNil(e.value)) {
                        dispatchLinkSelector({ type: 'SET_LINK', payload: e.value });
                        getFields(e.value);
                      }
                    }}
                    optionLabel="name"
                    optionValue="value"
                    title={datasetSchema.datasetSchemaName}
                    value={link}></ListBox>
                );
              })}
          </div>
          <div className={styles.selectedLinkFieldsWrapper}>
            <span htmlFor={'linkedTableLabel'}>{resources.messages['linkedTableLabel']}</span>
            <Dropdown
              appendTo={document.body}
              ariaLabel="linkedTableLabel"
              className={styles.fieldSelector}
              inputId="linkedTableLabel"
              name={resources.messages['linkedTableLabel']}
              onChange={e => dispatchLinkSelector({ type: 'SET_LINKED_TABLE_LABEL', payload: e.target.value })}
              optionLabel="name"
              options={linkedTableFields}
              placeholder={resources.messages['linkedTableLabel']}
              value={pkLinkedTableLabel}
            />
          </div>
          <div className={styles.selectedLinkFieldsWrapper}>
            <span htmlFor={'masterTableConditional'}>{resources.messages['masterTableConditional']}</span>
            <Dropdown
              appendTo={document.body}
              ariaLabel="masterTableConditional"
              className={styles.fieldSelector}
              inputId="masterTableConditional"
              name={resources.messages['masterTableConditional']}
              onChange={e => dispatchLinkSelector({ type: 'SET_MASTER_TABLE_CONDITIONAL', payload: e.target.value })}
              optionLabel="name"
              options={masterTableFields}
              placeholder={resources.messages['masterTableConditional']}
              value={pkMasterTableConditional}
            />
            <span htmlFor={'linkedTableConditional'}>{resources.messages['linkedTableConditional']}</span>
            <Dropdown
              appendTo={document.body}
              ariaLabel="linkedTableConditional"
              className={styles.fieldSelector}
              inputId="linkedTableConditional"
              name={resources.messages['linkedTableConditional']}
              onChange={e => dispatchLinkSelector({ type: 'SET_LINKED_TABLE_CONDITIONAL', payload: e.target.value })}
              optionLabel="name"
              options={linkedTableFields}
              placeholder={resources.messages['linkedTableConditional']}
              value={pkLinkedTableConditional}
            />
          </div>
          <div className={styles.selectedLinkWrapper}>
            <span className={styles.switchTextInput} htmlFor={'pkMustBeUsed_check'}>
              {resources.messages['pkValuesMustBeUsed']}
            </span>
            <Checkbox
              checked={pkMustBeUsed}
              id={'pkMustBeUsed_check'}
              inputId={'pkMustBeUsed_check'}
              label="Default"
              onChange={e => setPkMustBeUsed(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem' }}
            />
            <span className={styles.switchTextInput} htmlFor={'pkHasMultipleValues_check'}>
              {resources.messages['pkHasMultipleValues']}
            </span>
            <Checkbox
              checked={pkHasMultipleValues}
              id={'pkHasMultipleValues_check'}
              inputId={'pkHasMultipleValues_check'}
              label="Default"
              onChange={e => setPkHasMultipleValues(e.checked)}
              style={{ width: '70px', marginLeft: '0.5rem' }}
            />
          </div>
          <div className={styles.selectedLinkWrapper}>
            <span className={styles.selectedLinkLabel}>{`${resources.messages['selectedLink']}: `}</span>
            <span>{!isNil(link) ? link.name : ''}</span>
          </div>
        </Fragment>
      );
    };

    return (
      isVisible && (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          footer={linkSelectorDialogFooter}
          header={resources.messages['linkSelector']}
          modal={true}
          onHide={() => {
            onCancelSaveLink({
              link,
              pkMustBeUsed,
              pkHasMultipleValues,
              linkedTableLabel: pkLinkedTableLabel?.fieldSchemaId,
              linkedTableConditional: pkLinkedTableConditional?.fieldSchemaId,
              masterTableConditional: pkMasterTableConditional?.fieldSchemaId
            });
            setIsVisible(false);
          }}
          style={{ minWidth: '55%' }}
          visible={isVisible}
          zIndex={3003}>
          {isLoading ? <Spinner className={styles.positioning} /> : renderLinkSelector()}
        </Dialog>
      )
    );
  }
);

export { LinkSelector };
