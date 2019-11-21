import React, { Component } from 'react';
import classNames from 'classnames';
import ObjectUtils from 'ui/ObjectUtils';
import DomHandler from 'ui/DomHandler';
import { RowRadioButton } from './_components/RowRadioButton';
import { RowCheckbox } from 'ui/views/_components/DataTable/_components/RowCheckbox';

export class BodyCell extends Component {
  constructor(props) {
    super(props);
    this.state = {};
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
    if (event.which === 13 || event.which === 9) {
      this.switchCellToViewMode();
    }
  }

  onClick() {
    this.editingCellClick = true;

    if (this.props.editor && !this.state.editing) {
      this.setState({
        editing: true
      });

      if(this.props.onEditingToggle){
        this.props.onEditingToggle(true);
      }

      if (this.props.editorValidatorEvent === 'click') {
        this.bindDocumentEditListener();
      }
    }
  }

  onBlur() {
    if (this.state.editing && this.props.editorValidatorEvent === 'blur') {
      this.switchCellToViewMode();
      if(this.props.onEditingToggle){
        this.props.onEditingToggle(false);
      }
    }
  }

  onEditorFocus(event) {
    this.onClick(event);
  }

  bindDocumentEditListener() {
    if (!this.documentEditListener) {
      this.documentEditListener = event => {
        if (!this.editingCellClick) {
          this.switchCellToViewMode();
        }

        this.editingCellClick = false;
      };

      this.editingCellClick = false;

      document.addEventListener('click', this.documentEditListener);
    }
  }

  closeCell() {
    this.setState({
      editing: false
    });

    this.unbindDocumentEditListener();
  }

  switchCellToViewMode() {
    if (this.props.editorValidator) {
      let valid = this.props.editorValidator(this.props);
      if (valid) {
        this.closeCell();
      }
    } else {
      this.closeCell();
    }
  }

  unbindDocumentEditListener() {
    if (this.documentEditListener) {
      document.removeEventListener('click', this.documentEditListener);
      this.documentEditListener = null;
    }
  }

  componentDidUpdate() {
    if (this.container && this.props.editor) {
      if (this.state.editing) {
        let focusable = DomHandler.findSingle(this.container, 'input');
        if (focusable) {
          focusable.setAttribute('data-isCellEditing', true);
          focusable.focus();
        }

        this.keyHelper.tabIndex = -1;
      } else {
        setTimeout(() => {
          if (this.keyHelper) {
            this.keyHelper.removeAttribute('tabindex');
          }
        }, 50);
      }
    }
  }

  componentWillUnmount() {
    this.unbindDocumentEditListener();
  }

  render() {
    let content, header;
    let cellClassName = classNames(this.props.bodyClassName || this.props.className, {
      'p-selection-column': this.props.selectionMode,
      'p-editable-column': this.props.editor,
      'p-cell-editing': this.state.editing
    });

    if (this.props.expander) {
      let iconClassName = classNames('p-row-toggler-icon pi pi-fw p-clickable', {
        'pi-chevron-down': this.props.expanded,
        'pi-chevron-right': !this.props.expanded
      });
      content = (
        <button onClick={this.onExpanderClick} className="p-row-toggler p-link">
          <span className={iconClassName} />
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

      content = <i className={reorderIcon} />;
    } else {
      if (this.state.editing) {
        if (this.props.editor) content = this.props.editor(this.props);
        else throw new Error('Editor is not found on column.');
      } else {
        if (this.props.body) content = this.props.body(this.props.rowData, this.props);
        else content = ObjectUtils.resolveFieldData(this.props.rowData, this.props.field);
      }
    }

    if (this.props.responsive) {
      header = <span className="p-column-title">{this.props.header}</span>;
    }

    /* eslint-disable */
    let editorKeyHelper = this.props.editor && (
      <a
        tabIndex="0"
        ref={el => {
          this.keyHelper = el;
        }}
        className="p-cell-editor-key-helper p-hidden-accessible"
        onFocus={this.onEditorFocus}>
        <span />
      </a>
    );
    /* eslint-enable */

    return (
      <td
        ref={el => {
          this.container = el;
        }}
        className={cellClassName}
        style={this.props.bodyStyle || this.props.style}
        onClick={this.onClick}
        onKeyDown={this.onKeyDown}
        rowSpan={this.props.rowSpan}
        onBlur={this.onBlur}>
        {header}
        {editorKeyHelper}
        {content}
      </td>
    );
  }
}
