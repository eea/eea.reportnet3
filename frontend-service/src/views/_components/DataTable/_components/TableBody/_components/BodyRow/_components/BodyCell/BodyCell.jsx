import { Component } from 'react';
import classNames from 'classnames';
import ObjectUtils from 'views/_functions/PrimeReact/ObjectUtils';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

import styles from './BodyCell.module.scss';

import { Button } from 'views/_components/Button';
import ReactTooltip from 'react-tooltip';
import { RowRadioButton } from './_components/RowRadioButton';
import { RowCheckbox } from 'views/_components/DataTable/_components/RowCheckbox';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export class BodyCell extends Component {
  static contextType = ResourcesContext;

  constructor(props) {
    super(props);
    this.state = {
      editing: this.props.editing
    };

    this.onExpanderClick = this.onExpanderClick.bind(this);
    this.onClick = this.onClick.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onEditorFocus = this.onEditorFocus.bind(this);
  }

  onExpanderClick(event) {
    if (this.props.onRowToggle) {
      this.props.onRowToggle({
        originalEvent: event,
        data: this.props.rowData
      });
    }

    event.preventDefault();
  }

  onKeyDown(event) {
    if (this.props.editMode !== 'row') {
      if (event.which === 9) {
        // tab || enter
        this.switchCellToViewMode(true);
      }
      if (event.which === 27) {
        // escape
        this.switchCellToViewMode(false);
      }
    }
  }

  onClick() {
    if (this.props.editMode !== 'row') {
      this.editingCellClick = true;

      if (this.props.editor && !this.state.editing) {
        this.setState({
          editing: true
        });

        if (this.props.editorValidatorEvent === 'click') {
          this.bindDocumentEditListener();
        }
      }
    }
  }

  onBlur() {
    if (this.props.editMode !== 'row' && this.state.editing && this.props.editorValidatorEvent === 'blur') {
      this.switchCellToViewMode(true);
    }
  }

  onEditorFocus(event) {
    this.onClick(event);
  }

  bindDocumentEditListener() {
    if (!this.documentEditListener) {
      this.documentEditListener = event => {
        let selection = '';
        if (window.getSelection) {
          selection = window.getSelection();
        } else if (document.selection) {
          selection = document.selection.createRange();
        }

        if (selection.toString() !== '') {
          this.editingCellClick = true;
        }
        if (!this.editingCellClick) {
          this.switchCellToViewMode(true);
        }

        this.editingCellClick = false;
      };

      this.editingCellClick = false;

      document.addEventListener('click', this.documentEditListener);
    }
  }

  calculateRowDisabledQuickEdit() {
    return (
      (this.props.rowData[this.props.quickEditRowInfo.property] === this.props.quickEditRowInfo.updatedRow ||
        this.props.rowData[this.props.quickEditRowInfo.property] === this.props.quickEditRowInfo.deletedRow) &&
      this.props.quickEditRowInfo.condition
    );
  }

  checkEditorInvalid() {
    return this.props.quickEditRowInfo.requiredFields.some(field => this.props.rowData[field] === '');
  }

  closeCell() {
    this.setState({
      editing: false
    });

    this.unbindDocumentEditListener();
  }

  switchCellToViewMode(submit) {
    if (this.props.editorValidator && submit) {
      let valid = this.props.editorValidator(this.props);
      if (valid) {
        if (this.props.onEditorSubmit) {
          this.props.onEditorSubmit(this.props);
        }
        this.closeCell();
      } // as per previous version if not valid and another editor is open, keep invalid data editor open.
    } else {
      if (submit && this.props.onEditorSubmit) {
        this.props.onEditorSubmit(this.props);
      } else if (this.props.onEditorCancel) {
        this.props.onEditorCancel(this.props);
      }
      this.closeCell();
    }
  }

  unbindDocumentEditListener() {
    if (this.documentEditListener) {
      document.removeEventListener('click', this.documentEditListener);
      this.documentEditListener = null;
    }
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    if (nextProps.editMode === 'row' && nextProps.editing !== prevState.editing) {
      return {
        editing: nextProps.editing
      };
    }

    return null;
  }

  componentDidUpdate() {
    if (this.props.editMode !== 'row' && this.container && this.props.editor) {
      clearTimeout(this.tabindexTimeout);
      if (this.state.editing) {
        let focusable =
          DomHandler.findSingle(this.container, 'input') || DomHandler.findSingle(this.container, 'textarea');
        if (focusable && document.activeElement !== focusable && !focusable.hasAttribute('data-isCellEditing')) {
          focusable.setAttribute('data-isCellEditing', true);
          focusable.focus();
        }

        this.keyHelper.tabIndex = -1;
      } else {
        this.tabindexTimeout = setTimeout(() => {
          if (this.keyHelper) {
            this.keyHelper.setAttribute('tabindex', 0);
          }
        }, 50);
      }
    }
  }

  componentWillUnmount() {
    this.unbindDocumentEditListener();
  }

  render() {
    let content, header, editorKeyHelper;
    let cellClassName = classNames(this.props.bodyClassName || this.props.className, {
      'p-selection-column': this.props.selectionMode,
      'p-editable-column': this.props.editor,
      'p-cell-editing': this.state.editing && this.props.editor
    });

    if (this.props.expander) {
      let iconClassName = classNames('p-row-toggler-icon pi pi-fw p-clickable', {
        'pi-chevron-down': this.props.expanded,
        'pi-chevron-right': !this.props.expanded
      });
      content = (
        <button className="p-row-toggler p-link" onClick={this.onExpanderClick}>
          <span className={iconClassName}></span>
        </button>
      );
    } else if (this.props.selectionMode) {
      if (this.props.selectionMode === 'single')
        content = (
          <RowRadioButton
            onClick={this.props.onRadioClick}
            rowData={this.props.rowData}
            selected={this.props.selected}
          />
        );
      else
        content = (
          <RowCheckbox
            onClick={this.props.onCheckboxClick}
            rowData={this.props.rowData}
            selected={this.props.selected}
          />
        );
    } else if (this.props.rowReorder) {
      let reorderIcon = classNames('p-table-reorderablerow-handle', this.props.rowReorderIcon);

      content = <i className={reorderIcon}></i>;
    } else if (this.props.rowEditor) {
      if (this.state.editing) {
        content = (
          <div className={styles.actionTemplate}>
            <span data-for={`quickEditSaveTooltip${this.props.rowIndex}`} data-tip>
              <Button
                className={`${`p-button-rounded p-button-primary-transparent ${styles.editSaveRowButton}`} ${
                  !this.checkEditorInvalid() ? 'p-button-animated-blink' : ''
                }`}
                disabled={this.checkEditorInvalid()}
                icon="check"
                onClick={this.props.onRowEditSave}
              />
            </span>
            <span data-for={`quickEditCancelTooltip${this.props.rowIndex}`} data-tip>
              <Button
                className={`${`p-button-rounded p-button-secondary-transparent ${styles.editCancelRowButton}`} p-button-animated-blink`}
                icon="cancel"
                onClick={this.props.onRowEditCancel}
              />
            </span>

            <ReactTooltip
              border={true}
              className={styles.tooltip}
              effect="solid"
              id={`quickEditSaveTooltip${this.props.rowIndex}`}
              place="top">
              <span>
                {!this.checkEditorInvalid()
                  ? this.context.messages['save']
                  : this.context.messages['fcSubmitButtonDisabled']}
              </span>
            </ReactTooltip>
            <ReactTooltip
              border={true}
              className={styles.tooltip}
              effect="solid"
              id={`quickEditCancelTooltip${this.props.rowIndex}`}
              place="top">
              <span> {this.context.messages['cancel']} </span>
            </ReactTooltip>
          </div>
        );
      } else {
        content = (
          <div className={styles.actionTemplate}>
            <Button
              className={`${`p-button-rounded p-button-secondary-transparent ${styles.editRowButton}`} ${
                !this.calculateRowDisabledQuickEdit() ? 'p-button-animated-blink' : ''
              }`}
              disabled={this.props.quickEditRowInfo ? this.props.quickEditRowInfo.condition : false}
              icon={this.props.quickEditRowInfo && this.calculateRowDisabledQuickEdit() ? 'spinnerAnimate' : 'clock'}
              onClick={this.props.onRowEditInit}
              tooltip={this.context.messages['quickEdit']}
              tooltipOptions={{ position: 'top' }}
              type="button"
            />
            {this.props.body(this.props.rowData, this.props)}
          </div>
        );
      }
    } else {
      if (this.state.editing && this.props.editor) {
        content = this.props.editor(this.props);
      } else {
        if (this.props.body) content = this.props.body(this.props.rowData, this.props);
        else content = ObjectUtils.resolveFieldData(this.props.rowData, this.props.field);
      }
    }

    if (this.props.responsive) {
      header = <span className="p-column-title">{this.props.header}</span>;
    }

    if (this.props.editMode !== 'row') {
      /* eslint-disable */
      editorKeyHelper = this.props.editor && (
        <a
          tabIndex="0"
          ref={el => {
            this.keyHelper = el;
          }}
          className="p-cell-editor-key-helper p-hidden-accessible"
          onFocus={this.onEditorFocus}>
          <span></span>
        </a>
      );
      /* eslint-enable */
    }

    return (
      <td
        className={cellClassName}
        onBlur={this.onBlur}
        onClick={this.onClick}
        onKeyDown={this.onKeyDown}
        ref={el => {
          this.container = el;
        }}
        rowSpan={this.props.rowSpan}
        style={this.props.bodyStyle || this.props.style}>
        {header}
        {editorKeyHelper}
        {content}
      </td>
    );
  }
}
