import { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import pick from 'lodash/pick';
import uniqueId from 'lodash/uniqueId';

import styles from './LinkSelector.module.scss';

import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { ListBox } from 'views/DatasetDesigner/_components/ListBox';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';
import { ReferenceDataflowService } from 'services/ReferenceDataflowService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { linkSelectorReducer } from './_functions/Reducers/linkSelectorReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

const LinkSelector = withRouter(
  ({
    datasetSchemaId,
    fieldId,
    fields,
    hasMultipleValues = false,
    isExternalLink,
    isLinkSelectorVisible,
    isReferenceDataset,
    linkedTableConditional,
    linkedTableLabel,
    masterTableConditional,
    match,
    mustBeUsed = false,
    onCancelSaveLink,
    onHideSelector,
    onSaveLink,
    selectedLink,
    tableSchemaId
  }) => {
    const resources = useContext(ResourcesContext);

    const [linkSelectorState, dispatchLinkSelector] = useReducer(linkSelectorReducer, {
      link: {
        ...selectedLink,
        referencedField: !isNil(selectedLink)
          ? pick(
              selectedLink.referencedField,
              'dataflowId',
              'datasetSchemaId',
              'fieldSchemaId',
              'fieldSchemaName',
              'tableSchemaId',
              'tableSchemaName'
            )
          : null
      },
      linkedTableFields: [],
      pkLinkedTableLabel: {},
      pkLinkedTableConditional: {},
      masterTableConditional: {},
      masterTableFields: [],
      selectedReferenceDataflow: {}
    });

    const {
      link,
      linkedTableFields,
      pkLinkedTableLabel,
      pkLinkedTableConditional,
      pkMasterTableConditional,
      masterTableFields
    } = linkSelectorState;

    const [referenceDataflows, setReferenceDataflows] = useState([]);
    const [datasetSchemas, setDatasetSchemas] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isVisible, setIsVisible] = useState(isLinkSelectorVisible);
    const [isSaved, setIsSaved] = useState(false);
    const [pkHasMultipleValues, setPkHasMultipleValues] = useState(hasMultipleValues);
    const [pkMustBeUsed, setPkMustBeUsed] = useState(mustBeUsed);

    const {
      params: { dataflowId }
    } = match;

    useEffect(() => {
      setIsLoading(true);
      const getReferenceDataflows = async () => {
        const data = await ReferenceDataflowService.getAll();
        const filteredDataflows = data.filter(
          dataflow => dataflow.id !== parseFloat(dataflowId) && TextUtils.areEquals(dataflow.status, 'DRAFT')
        );
        setReferenceDataflows(filteredDataflows);
      };

      getReferenceDataflows();
      setIsLoading(false);
    }, []);

    useEffect(() => {
      if (isExternalLink && !isEmpty(referenceDataflows) && !isNil(selectedLink) && !isEmpty(selectedLink)) {
        dispatchLinkSelector({
          type: 'SET_REFERENCE_DATAFLOW',
          payload:
            referenceDataflows.find(
              referenceDataflow => referenceDataflow.id === selectedLink.referencedField.dataflowId
            ) || {}
        });
      }
    }, [referenceDataflows]);

    useEffect(() => {
      if (!isExternalLink) {
        getDatasetSchemas();
      } else if (isExternalLink && !isEmpty(linkSelectorState.selectedReferenceDataflow)) {
        getDatasetSchemas();
      }
    }, [linkSelectorState.selectedReferenceDataflow]);

    const getDatasetSchemas = async () => {
      setIsLoading(true);
      let datasetSchemasDTO;
      if (isExternalLink && !isEmpty(linkSelectorState.selectedReferenceDataflow)) {
        datasetSchemasDTO = await DataflowService.getSchemas(linkSelectorState.selectedReferenceDataflow.id);
      } else {
        datasetSchemasDTO = await DataflowService.getSchemas(dataflowId);
      }
      setIsLoading(false);
      if (isReferenceDataset) {
        const filteredDatasets = datasetSchemasDTO.filter(datasetSchemaDTO => datasetSchemaDTO.referenceDataset);
        setDatasetSchemas(filteredDatasets);
      } else {
        setDatasetSchemas(datasetSchemasDTO);
      }
    };

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
            if (fieldId === '-1' || isNil(link) || isNil(link.referencedField)) {
              onHideSelector();
            } else {
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
        ?.tables.find(table =>
          !isExternalLink
            ? table.tableSchemaId === field.referencedField.tableSchemaId
            : table.tableSchemaName === field.referencedField.tableSchemaName
        );

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

      if (isExternalLink && !isNil(fields)) {
        masterFields = getMasterFields(fields);
      } else {
        const masterTable = datasetSchemas
          .find(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaId)
          ?.tables.find(table => table.tableSchemaId === tableSchemaId);
        masterFields = getMasterFields(masterTable?.records[0].fields);
      }

      masterFields?.unshift({
        name: resources.messages['noneCodelist'],
        fieldSchemaId: ''
      });

      dispatchLinkSelector({ type: 'SET_LINKED_AND_MASTER_FIELDS', payload: { linkedFields, masterFields } });
    };

    const getMasterFields = fields => {
      const masterFields = fields
        ?.filter(
          field =>
            !field.pk &&
            !['ATTACHMENT', 'POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(
              field.type.toUpperCase()
            )
        )
        .map(field => {
          return { fieldSchemaId: field.fieldId, name: field.name };
        });
      return masterFields;
    };

    const getOptions = datasetSchema =>
      datasetSchema.tables.map(table => {
        const hasPK = !isUndefined(table.records[0].fields.filter(field => field.pk === true)[0]);
        if (hasPK && table.tableSchemaId !== tableSchemaId) {
          const pkField = table.records[0].fields.filter(field => field.pk === true)[0];
          if (
            !['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(pkField.type)
          ) {
            const linkObj = {
              name: `${table.tableSchemaName} - ${pkField.name}`,
              value: `${table.tableSchemaName} - ${pkField.fieldId}`,
              referencedField: {
                fieldSchemaId: pkField.fieldId,
                datasetSchemaId: datasetSchema.datasetSchemaId,
                tableSchemaId: table.tableSchemaId
              },
              disabled: false
            };
            if (isExternalLink) {
              linkObj.referencedField.dataflowId = linkSelectorState.selectedReferenceDataflow.id;
              linkObj.referencedField.fieldSchemaName = pkField.name;
              linkObj.referencedField.tableSchemaName = table.tableSchemaName;
            }
            return linkObj;
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

    const dataflowsTemplate = option => {
      return (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ margin: '.5em .25em 0 0.5em' }}>{option.name}</span>
          <span style={{ margin: '.5em .25em 0 0.5em', fontSize: '8pt', fontStyle: 'italic' }}>
            {option.description}
          </span>
        </div>
      );
    };

    const renderExternalLinkSelector = () => {
      return (
        <div className={styles.referenceDataflowsWrapper}>
          <div
            className={`${styles.referenceDataflowsDropdownTitle} ${
              !isEmpty(linkSelectorState.selectedReferenceDataflow) && styles.selectedReferenceDataflowsDropdownTitle
            }`}>
            {!isEmpty(linkSelectorState.selectedReferenceDataflow) && (
              <div>
                <span
                  className={
                    styles.selectedReferenceDataflowLabel
                  }>{`${resources.messages['selectedReferenceDataflow']}: `}</span>
                <span>{linkSelectorState.selectedReferenceDataflow.name}</span>
              </div>
            )}
            <div className={styles.referenceDataflowsDropdownWrapper}>
              <label>{resources.messages['referenceDataflows']}</label>
              <Button
                className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
                icon={'infoCircle'}
                tooltip={resources.messages['referenceDataflowsDraftInfo']}
                tooltipOptions={{ position: 'top' }}
              />
              <Dropdown
                ariaLabel={'referenceDataflows'}
                className={styles.referenceDataflowsDropdown}
                filter={true}
                filterBy="name,description"
                filterPlaceholder={resources.messages['linkFilterPlaceholder']}
                itemTemplate={dataflowsTemplate}
                name="referenceDataflowsDropdown"
                onChange={e => dispatchLinkSelector({ type: 'SET_REFERENCE_DATAFLOW', payload: e.target.value })}
                optionLabel="name"
                options={referenceDataflows}
                placeholder={resources.messages['manageRolesDialogDropdownPlaceholder']}
                showFilterClear={true}
                value={linkSelectorState.selectedReferenceDataflow}
              />
            </div>
          </div>
          {isEmpty(linkSelectorState.selectedReferenceDataflow) ? (
            <p className={styles.chooseReferenceDataflowText}>
              {resources.messages['externalLinkDialogNoReferenceDataflowMessage']}
            </p>
          ) : (
            renderLinkSelector()
          )}
        </div>
      );
    };

    const renderLinkSelector = () => {
      if (!datasetSchemas.length) {
        return <p className={styles.chooseReferenceDataflowText}>{resources.messages['emptyDatasetSchemas']}</p>;
      } else {
        if (isExternalLink) {
          const tabSchema = datasetSchemas
            .find(datasetSchema => datasetSchema.datasetSchemaId === link.referencedField?.datasetSchemaId)
            ?.tables.find(table => table.tableSchemaName === link.referencedField?.tableSchemaName);
          if (!isNil(tabSchema)) {
            link.referencedField.tableSchemaId = tabSchema.tableSchemaId;
          }
        }
        return (
          <Fragment>
            <div className={`${styles.schemaWrapper} ${isExternalLink && styles.referenceDataflowSchemaWrapper}`}>
              {!isUndefined(datasetSchemas) &&
                !isEmpty(datasetSchemas) &&
                datasetSchemas.map(datasetSchema => {
                  return (
                    <ListBox
                      key={uniqueId('datasetSchema_')}
                      onChange={e => {
                        if (!isNil(e.value)) {
                          dispatchLinkSelector({ type: 'SET_LINK', payload: e.value });
                          getFields(e.value);
                        }
                      }}
                      optionLabel="name"
                      optionValue="value"
                      options={getOptions(datasetSchema)}
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
                id="pkMustBeUsed_check"
                inputId="pkMustBeUsed_check"
                label="Default"
                onChange={e => setPkMustBeUsed(e.checked)}
                style={{ width: '70px', marginLeft: '0.5rem' }}
              />
              <span className={styles.switchTextInput} htmlFor={'pkHasMultipleValues_check'}>
                {resources.messages['pkHasMultipleValues']}
              </span>
              <Checkbox
                checked={pkHasMultipleValues}
                id="pkHasMultipleValues_check"
                inputId="pkHasMultipleValues_check"
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
      }
    };

    return (
      isVisible && (
        <Dialog
          blockScroll={false}
          contentStyle={{ overflow: 'auto' }}
          footer={linkSelectorDialogFooter}
          header={isExternalLink ? resources.messages['externalLinkSelector'] : resources.messages['linkSelector']}
          modal={true}
          onHide={() => {
            if (fieldId === '-1' || isNil(link) || isNil(link.referencedField)) {
              onHideSelector();
            } else {
              onCancelSaveLink({
                link,
                pkMustBeUsed,
                pkHasMultipleValues,
                linkedTableLabel: pkLinkedTableLabel?.fieldSchemaId,
                linkedTableConditional: pkLinkedTableConditional?.fieldSchemaId,
                masterTableConditional: pkMasterTableConditional?.fieldSchemaId
              });
            }
            setIsVisible(false);
          }}
          style={{ width: '65%' }}
          visible={isVisible}
          zIndex={3003}>
          {isLoading ? (
            <Spinner className={styles.positioning} />
          ) : isExternalLink ? (
            renderExternalLinkSelector()
          ) : (
            renderLinkSelector()
          )}
        </Dialog>
      )
    );
  }
);

export { LinkSelector };
