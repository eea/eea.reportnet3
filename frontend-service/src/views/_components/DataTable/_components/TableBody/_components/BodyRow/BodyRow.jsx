import { Component, Children } from 'react';
import classNames from 'classnames';
import { BodyCell } from './_components/BodyCell';
import DomHandler from 'views/_functions/PrimeReact/DomHandler';

export class BodyRow extends Component {
  constructor(props) {
    super(props);
    this.state = {
      editing: false
    };

    this.onClick = this.onClick.bind(this);
    this.onDoubleClick = this.onDoubleClick.bind(this);
    this.onTouchEnd = this.onTouchEnd.bind(this);
    this.onRightClick = this.onRightClick.bind(this);
    this.onMouseDown = this.onMouseDown.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onRowEditInit = this.onRowEditInit.bind(this);
    this.onRowEditSave = this.onRowEditSave.bind(this);
    this.onRowEditCancel = this.onRowEditCancel.bind(this);
  }

  onClick(event) {
    if (this.props.onClick) {
      this.props.onClick({
        originalEvent: event,
        data: this.props.rowData,
        index: this.props.rowIndex
      });
    }
  }

  onDoubleClick(event) {
    if (this.props.onDoubleClick) {
      this.props.onDoubleClick({
        originalEvent: event,
        data: this.props.rowData,
        index: this.props.rowIndex
      });
    }
  }

  onTouchEnd(event) {
    if (this.props.onTouchEnd) {
      this.props.onTouchEnd(event);
    }
  }

  onRightClick(event) {
    if (this.props.onRightClick) {
      this.props.onRightClick({
        originalEvent: event,
        data: this.props.rowData,
        index: this.props.rowIndex
      });
    }
  }

  onMouseDown(event) {
    if (DomHandler.hasClass(event.target, 'p-table-reorderablerow-handle')) event.currentTarget.draggable = true;
    else event.currentTarget.draggable = false;
  }

  onDragEnd(event) {
    if (this.props.onDragEnd) {
      this.props.onDragEnd(event);
    }
    event.currentTarget.draggable = false;
  }

  onDragOver(event) {
    if (this.props.onDragOver) {
      this.props.onDragOver({
        originalEvent: event,
        rowElement: this.container
      });
    }
    event.preventDefault();
  }

  onDragLeave(event) {
    if (this.props.onDragLeave) {
      this.props.onDragLeave({
        originalEvent: event,
        rowElement: this.container
      });
    }
  }

  onDrop(event) {
    if (this.props.onDrop) {
      this.props.onDrop({
        originalEvent: event,
        rowElement: this.container
      });
    }
    event.preventDefault();
  }

  onKeyDown(event) {
    if (this.props.selectionMode && this.props.editMode !== 'cell') {
      const row = event.target;

      switch (event.which) {
        //down arrow
        case 40:
          let nextRow = this.findNextSelectableRow(row);
          if (nextRow) {
            nextRow.focus();
          }
          event.preventDefault();
          break;
        //up arrow
        case 38:
          let prevRow = this.findPrevSelectableRow(row);
          if (prevRow) {
            prevRow.focus();
          }
          event.preventDefault();
          break;
        //enter
        case 13:
          this.onClick(event);
          break;
        case 9:
          //this.findNextSelectableCell(row);
          break;
        default:
          //no op
          break;
      }
    }
  }

  // findNextSelectableCell(cell) {
  //   let nextCell = cell.nextElementSibling;
  // }

  findNextSelectableRow(row) {
    let nextRow = row.nextElementSibling;
    if (nextRow) {
      if (DomHandler.hasClass(nextRow, 'p-datatable-row')) return nextRow;
      else return this.findNextSelectableRow(nextRow);
    } else {
      return null;
    }
  }

  findPrevSelectableRow(row) {
    let prevRow = row.previousElementSibling;
    if (prevRow) {
      if (DomHandler.hasClass(prevRow, 'p-datatable-row')) return prevRow;
      else return this.findPrevSelectableRow(prevRow);
    } else {
      return null;
    }
  }

  onRowEditInit(event) {
    if (this.props.onRowEditInit) {
      this.props.onRowEditInit({
        originalEvent: event,
        data: this.props.rowData
      });
    }

    this.setState({
      editing: true
    });

    event.preventDefault();
  }

  onRowEditSave(event) {
    let valid = true;

    if (this.props.rowEditorValidator) {
      valid = this.props.rowEditorValidator(this.props.rowData);
    }

    if (this.props.onRowEditSave) {
      this.props.onRowEditSave({
        originalEvent: event,
        data: this.props.rowData
      });
    }

    this.setState({
      editing: !valid
    });

    event.preventDefault();
  }

  onRowEditCancel(event) {
    if (this.props.onRowEditCancel) {
      this.props.onRowEditCancel({
        originalEvent: event,
        data: this.props.rowData,
        index: this.props.rowIndex
      });
    }

    this.setState({
      editing: false
    });

    event.preventDefault();
  }

  render() {
    let columns = Children.toArray(this.props.children);
    let conditionalStyles = {
      'p-highlight': this.props.selected,
      'p-highlight-contextmenu': this.props.contextMenuSelected
    };

    if (this.props.rowClassName) {
      let rowClassNameCondition = this.props.rowClassName(this.props.rowData);
      conditionalStyles = { ...conditionalStyles, ...rowClassNameCondition };
    }
    let className = classNames('p-datatable-row', conditionalStyles);
    let hasRowSpanGrouping = this.props.rowGroupMode === 'rowspan';
    let cells = [];

    for (let i = 0; i < columns.length; i++) {
      let column = columns[i];
      let rowSpan;
      if (hasRowSpanGrouping) {
        if (this.props.sortField === column.props.field) {
          if (this.props.groupRowSpan) {
            rowSpan = this.props.groupRowSpan;
            className += ' p-datatable-rowspan-group';
          } else {
            continue;
          }
        }
      }

      let cell = (
        <BodyCell
          key={i}
          {...column.props}
          editMode={this.props.editMode}
          editing={this.state.editing}
          expanded={this.props.expanded}
          onCheckboxClick={this.props.onCheckboxClick}
          onRadioClick={this.props.onRadioClick}
          onRowEditCancel={this.onRowEditCancel}
          onRowEditInit={this.onRowEditInit}
          onRowEditSave={this.onRowEditSave}
          onRowToggle={this.props.onRowToggle}
          quickEditRowInfo={this.props.quickEditRowInfo}
          responsive={this.props.responsive}
          rowData={this.props.rowData}
          rowIndex={this.props.rowIndex}
          rowSpan={rowSpan}
          selected={this.props.selected}
          value={this.props.value}
        />
      );

      cells.push(cell);
    }

    return (
      <tr
        className={className}
        onClick={this.onClick}
        onContextMenu={this.onRightClick}
        onDoubleClick={this.onDoubleClick}
        onDragEnd={this.onDragEnd}
        onDragLeave={this.onDragLeave}
        onDragOver={this.onDragOver}
        onDragStart={this.props.onDragStart}
        onDrop={this.onDrop}
        onKeyDown={this.onKeyDown}
        onMouseDown={this.onMouseDown}
        onTouchEnd={this.onTouchEnd}
        ref={el => {
          this.container = el;
        }}
        style={{ height: this.props.virtualRowHeight }}
        tabIndex={this.props.selectionMode ? '0' : null}>
        {cells}
      </tr>
    );
  }
}
