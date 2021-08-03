import { Fragment, useContext, useEffect, useRef, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './ManageBusinessDataflow.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'ui/views/_components/Button';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { ErrorMessage } from 'ui/views/_components/ErrorMessage';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'ui/views/_components/InputText';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import ReactTooltip from 'react-tooltip';

import { BusinessDataflowService } from 'core/services/BusinessDataflow';
import { RepresentativeService } from 'core/services/Representative';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

import { UserService } from 'core/services/User';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const ManageBusinessDataflow = ({
  dataflowId,
  history,
  isEditing = false,
  isVisible,
  manageDialogs,
  metadata,
  obligation,
  onCreateDataflow,
  onEditDataflow,
  resetObligations
}) => {
  const dialogName = isEditing ? 'isEditDialogVisible' : 'isBusinessDataflowDialogVisible';
  const INPUT_MAX_LENGTH = 255;
  const isDesign = TextUtils.areEquals(metadata?.status, config.dataflowStatus.DESIGN);

  const { hideLoading, showLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [deleteInput, setDeleteInput] = useState('');
  const [selectedGroup, setSelectedGroup] = useState({});
  const [selectedFmeUser, setSelectedFmeUser] = useState({});
  const [description, setDescription] = useState(isEditing ? metadata.description : '');
  const [groupOfCompanies, setGroupOfCompanies] = useState([]);
  const [fmeUsers, setFmeUsers] = useState([]);

  const [errors, setErrors] = useState({
    description: { hasErrors: false, message: '' },
    name: { hasErrors: false, message: '' }
  });
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [name, setName] = useState(isEditing ? metadata.name : '');
  const [pinDataflow, setPinDataflow] = useState(false);

  const deleteInputRef = useRef(null);
  const inputRef = useRef(null);

  useInputTextFocus(isVisible, inputRef);
  useInputTextFocus(isDeleteDialogVisible, deleteInputRef);

  const getGroupOfCompaniesList = async () => {
    try {
      const providerTypes = await RepresentativeService.getGroupCompanies();
      setGroupOfCompanies(providerTypes.data);
    } catch (error) {
      console.error(error);
    }
  };

  const getFmeUsersList = async () => {
    try {
      const fmeUsersList = await RepresentativeService.getFmeUsers();
      setFmeUsers(fmeUsersList.data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    getGroupOfCompaniesList();
    getFmeUsersList();
  }, []);

  const checkErrors = () => {
    let hasErrors = false;
    if (description.length > INPUT_MAX_LENGTH) {
      handleErrors({
        field: 'description',
        hasErrors: true,
        message: resources.messages['dataflowDescriptionValidationMax']
      });
      hasErrors = true;
    }

    if (name.length > INPUT_MAX_LENGTH) {
      handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameValidationMax'] });
      hasErrors = true;
    }

    return hasErrors;
  };

  const handleErrors = ({ field, hasErrors, message }) => {
    setErrors(prevState => ({ ...prevState, [field]: { message, hasErrors } }));
  };

  const onSelectGroup = group => setSelectedGroup(group);
  const onSelectFmeUser = fmeUser => setSelectedFmeUser(fmeUser);

  const onDeleteDataflow = async () => {
    setIsDeleteDialogVisible(false);
    showLoading();
    try {
      const response = await BusinessDataflowService.deleteReferenceDataflow(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
        notificationContext.add({ type: 'DATAFLOW_DELETE_SUCCESS' });
      }
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
    } finally {
      hideLoading();
    }
  };

  const onManageBusinessDataflow = async () => {
    if (checkErrors()) return;

    try {
      setIsSending(true);
      if (isEditing) {
        const { status } = await BusinessDataflowService.edit(
          dataflowId,
          description,
          name,
          selectedGroup.dataProviderGroupId,
          selectedFmeUser.id
        );

        if (status >= 200 && status <= 299) {
          manageDialogs(dialogName, false);
          onEditDataflow(name, description);
        }
      } else {
        const { data, status } = await BusinessDataflowService.create(
          name,
          description,
          obligation.id,
          selectedGroup.dataProviderGroupId,
          selectedFmeUser.id
        );
        if (status >= 200 && status <= 299) {
          if (pinDataflow) {
            const inmUserProperties = { ...userContext.userProps };
            inmUserProperties.pinnedDataflows.push(data.toString());

            const response = await UserService.updateAttributes(inmUserProperties);
            if (!isNil(response) && response.status >= 200 && response.status <= 299) {
              userContext.onChangePinnedDataflows(inmUserProperties.pinnedDataflows);
            }
          }
          onCreateDataflow('isBusinessDataflowDialogVisible');
        }
      }
    } catch (error) {
      if (TextUtils.areEquals(error?.response?.data, 'Dataflow name already exists')) {
        handleErrors({ field: 'name', hasErrors: true, message: resources.messages['dataflowNameExists'] });
        notificationContext.add({ type: 'DATAFLOW_NAME_EXISTS' });
      } else {
        const notification = isEditing
          ? { type: 'BUSINESS_DATAFLOW_UPDATING_ERROR', content: { dataflowId, dataflowName: name } }
          : { type: 'BUSINESS_DATAFLOW_CREATION_ERROR', content: { dataflowName: name } };

        notificationContext.add(notification);
      }
    } finally {
      setIsSending(false);
    }
  };

  const renderDialogFooter = () => (
    <Fragment>
      <div className="p-toolbar-group-left">
        {!isEditing && (
          <div className={styles.checkboxWrapper}>
            <Checkbox
              ariaLabel={resources.messages['pinDataflow']}
              checked={pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() => setPinDataflow(!pinDataflow)}
              role="checkbox"
            />
            <label>
              <span onClick={() => setPinDataflow(!pinDataflow)}>{resources.messages['pinDataflow']}</span>
            </label>
            <FontAwesomeIcon
              aria-hidden={false}
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              data-for="pinDataflow"
              data-tip
              icon={AwesomeIcons('infoCircle')}
            />
            <ReactTooltip border={true} className={styles.tooltip} effect="solid" id="pinDataflow" place="top">
              <span>{resources.messages['pinDataflowMessage']}</span>
            </ReactTooltip>
          </div>
        )}
        {isEditing && isDesign && (
          <Button
            className="p-button-danger p-button-animated-blink"
            icon="trash"
            label={resources.messages['deleteDataflowButton']}
            onClick={() => setIsDeleteDialogVisible(true)}
          />
        )}
      </div>
      <Button
        className={`p-button-primary ${
          !isEmpty(name) &&
          !isEmpty(description) &&
          !isNil(obligation.id) &&
          !isNil(selectedFmeUser.id) &&
          !isNil(selectedGroup.dataProviderGroupId) &&
          !isSending &&
          'p-button-animated-blink'
        }`}
        disabled={
          isEmpty(name) ||
          isEmpty(description) ||
          isNil(obligation.id) ||
          isNil(selectedFmeUser.dataProviderGroupId) || // Todo change to userId
          isNil(selectedGroup.dataProviderGroupId) ||
          isSending
        }
        icon={isSending ? 'spinnerAnimate' : isEditing ? 'check' : 'plus'}
        label={isEditing ? resources.messages['save'] : resources.messages['create']}
        onClick={() => onManageBusinessDataflow()}
      />
      <Button
        className={`p-button-secondary button-right-aligned p-button-animated-blink ${styles.cancelButton}`}
        icon={'cancel'}
        label={isEditing ? resources.messages['cancel'] : resources.messages['close']}
        onClick={() => {
          resetObligations();
          manageDialogs(dialogName, false);
        }}
      />
    </Fragment>
  );

  return (
    <Fragment>
      <Dialog
        footer={renderDialogFooter()}
        header={
          isEditing
            ? resources.messages['editBusinessDataflowDialogHeader']
            : resources.messages['createBusinessDataflowDialogHeader']
        }
        onHide={() => {
          resetObligations();
          manageDialogs(dialogName, false);
        }}
        visible={isVisible}>
        <div className={`formField ${errors.name.hasErrors ? 'error' : ''}`}>
          <InputText
            id="dataflowName"
            onChange={event => setName(event.target.value)}
            onFocus={() => handleErrors({ field: 'name', hasErrors: false, message: '' })}
            placeholder={resources.messages['createDataflowName']}
            ref={inputRef}
            value={name}
          />
          {!isEmpty(errors.name.message) && <ErrorMessage message={errors.name.message} />}
        </div>

        <div className={`formField ${errors.description.hasErrors ? 'error' : ''}`}>
          <InputTextarea
            className={styles.inputTextArea}
            id="dataflowDescription"
            onChange={event => setDescription(event.target.value)}
            onFocus={() => handleErrors({ field: 'description', hasErrors: false, message: '' })}
            placeholder={resources.messages['createDataflowDescription']}
            rows={10}
            value={description}
          />
          {!isEmpty(errors.description.message) && <ErrorMessage message={errors.description.message} />}
        </div>
        <div className={styles.dropdownsWrapper}>
          <Dropdown
            appendTo={document.body}
            ariaLabel="groupOfCompanies"
            className={styles.groupOfCompaniesWrapper}
            name="groupOfCompanies"
            onChange={event => onSelectGroup(event.target.value)}
            onFocus={() => handleErrors({ field: 'groupOfCompanies', hasErrors: false, message: '' })}
            optionLabel="label"
            options={groupOfCompanies}
            placeholder={resources.messages[`selectGroupOfCompanies`]}
            value={selectedGroup}
          />

          <Dropdown
            appendTo={document.body}
            ariaLabel="fmeUsers"
            className={styles.fmeUsersWrapper}
            name="fmeUsers"
            onChange={event => onSelectFmeUser(event.target.value)}
            onFocus={() => handleErrors({ field: 'fmeUsers', hasErrors: false, message: '' })}
            optionLabel="label" // Todo change to userName
            options={fmeUsers}
            placeholder={resources.messages[`selectFmeUser`]}
            value={selectedFmeUser}
          />
        </div>
        <div className={`${styles.search}`}>
          <Button
            icon="search"
            label={resources.messages['searchObligations']}
            onClick={() => manageDialogs('isReportingObligationsDialogVisible', true)}
          />
          <InputText
            className={`${styles.searchInput} ${errors?.obligation?.hasErrors ? styles.searchErrors : ''}`}
            id="obligation"
            placeholder={resources.messages['associatedObligation']}
            readOnly={true}
            type="text"
            value={obligation.title}
          />
          <label className="srOnly" htmlFor="obligation">
            {resources.messages['searchObligations']}
          </label>
        </div>
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={!TextUtils.areEquals(deleteInput, metadata.name)}
          header={resources.messages['deleteBusinessDataflowDialogHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={onDeleteDataflow}
          onHide={() => setIsDeleteDialogVisible(false)}
          visible={isDeleteDialogVisible}>
          <p>{resources.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
                dataflowName: metadata.name
              })
            }}></p>
          <p>
            <InputText
              className={`${styles.inputText}`}
              id="deleteDataflow"
              maxLength={255}
              name={resources.messages['deleteDataflowButton']}
              onChange={event => setDeleteInput(event.target.value)}
              ref={deleteInputRef}
              value={deleteInput}
            />
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
