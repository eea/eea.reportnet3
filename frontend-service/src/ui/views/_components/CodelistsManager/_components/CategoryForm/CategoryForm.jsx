import React, { useContext, useState } from 'react';
import { capitalize, isNull, isUndefined } from 'lodash';

import styles from './CategoryForm.module.css';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';

import { CodelistCategoryService } from 'core/services/CodelistCategory';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const CategoryForm = ({
  checkCategoryDuplicates,
  checkNoCodelistEditing,
  columns,
  isEditionModeOn,
  isIncorrect,
  isInDesign,
  onLoadCategories,
  onToggleIncorrect
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const [newCategory, setNewCategory] = useState({ shortCode: '', description: '' });
  const [newCategoryVisible, setNewCategoryVisible] = useState(false);

  const onChangeCategoryForm = (property, value) => {
    const inmNewCategory = { ...newCategory };
    inmNewCategory[property] = value;
    setNewCategory(inmNewCategory);
  };

  const onSaveCategory = async () => {
    try {
      const response = await CodelistCategoryService.addById(newCategory.shortCode, newCategory.description);
      if (response.status >= 200 && response.status <= 299) {
        onLoadCategories();
      }
    } catch (error) {
      console.error(error);
      notificationContext.add({
        type: 'CODELIST_CATEGORY_SERVICE_ADD_BY_ID_ERROR'
      });
    } finally {
      setNewCategoryVisible(false);
    }
  };

  const categoryDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        className="p-button-primary"
        disabled={isIncorrect || Object.values(newCategory).includes('')}
        icon="save"
        label={resources.messages['save']}
        onClick={() => {
          onSaveCategory();
        }}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resources.messages['cancel']}
        onClick={() => {
          onToggleIncorrect(false);
          setNewCategory({ shortCode: '', description: '' });
          setNewCategoryVisible(false);
        }}
      />
    </div>
  );

  const addCategoryForm = columns.map(column => {
    return (
      <React.Fragment key={column}>
        <span className={`${styles.categoryInput} p-float-label`}>
          <InputText
            className={isIncorrect && column === 'shortCode' ? styles.categoryIncorrectInput : null}
            id={`${column}Input`}
            onBlur={() =>
              column === 'shortCode' ? onToggleIncorrect(checkCategoryDuplicates(newCategory[column])) : null
            }
            onChange={e => onChangeCategoryForm(column, e.target.value)}
            // required={true}
            value={
              isUndefined(newCategory) || isNull(newCategory[column]) || isUndefined(newCategory[column])
                ? ''
                : newCategory[column]
            }
          />
          <label htmlFor={`${column}Input`}>
            {column === 'shortCode' ? resources.messages['categoryShortCode'] : capitalize(column)}
          </label>
        </span>
      </React.Fragment>
    );
  });

  const renderDialog = (
    <Dialog
      className="edit-table"
      blockScroll={false}
      footer={categoryDialogFooter}
      header={resources.messages['addNewCategory']}
      modal={true}
      onHide={() => setNewCategoryVisible(false)}
      style={{ width: '50%' }}
      visible={newCategoryVisible}>
      <div className="p-grid p-fluid">{addCategoryForm}</div>
    </Dialog>
  );

  return (
    <React.Fragment>
      <Button
        className={isEditionModeOn ? styles.refreshCategoriesButton : null}
        disabled={!checkNoCodelistEditing()}
        icon="add"
        label={resources.messages['newCategory']}
        onClick={() => setNewCategoryVisible(true)}
        style={{ marginRight: !checkNoCodelistEditing() ? '0.5rem' : '1.5rem' }}
        visible={!isInDesign || isEditionModeOn}
      />
      {renderDialog}
    </React.Fragment>
  );
};

export { CategoryForm };
