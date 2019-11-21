import React, { Component } from 'react';
import classNames from 'classnames';
import { BodyCell } from './_components/BodyCell';
import DomHandler from 'ui/DomHandler';

export class BodyRow extends Component {
  constructor(props) {
    super(props);
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
    if (this.props.selectionMode) {
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
        //console.log('Tab!');
        //this.findNextSelectableCell(row);
        default:
          //no op
          break;
      }
    }
  }

  findNextSelectableCell(cell) {
    let nextCell = cell.nextElementSibling;
    //console.log(nextCell);
  }

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

  render() {
    let columns = React.Children.toArray(this.props.children);
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
          if (this.props.groupRowSpan) rowSpan = this.props.groupRowSpan;
          else continue;
        }
      }

      let cell = (
        <BodyCell
          key={i}
          {...column.props}
          value={this.props.value}
          rowSpan={rowSpan}
          rowData={this.props.rowData}
          rowIndex={this.props.rowIndex}
          onEditingToggle={this.props.onEditingToggle}
          onRowToggle={this.props.onRowToggle}
          expanded={this.props.expanded}
          onRadioClick={this.props.onRadioClick}
          onCheckboxClick={this.props.onCheckboxClick}
          responsive={this.props.responsive}
          selected={this.props.selected}
        />
      );

      cells.push(cell);
    }

    return (
      <tr
        tabIndex={this.props.selectionMode ? '0' : null}
        ref={el => {
          this.container = el;
        }}
        className={className}
        onClick={this.onClick}
        onDoubleClick={this.onDoubleClick}
        onTouchEnd={this.onTouchEnd}
        onContextMenu={this.onRightClick}
        onMouseDown={this.onMouseDown}
        onDragStart={this.props.onDragStart}
        onDragEnd={this.onDragEnd}
        onDragOver={this.onDragOver}
        onDragLeave={this.onDragLeave}
        onDrop={this.onDrop}
        style={{ height: this.props.virtualRowHeight }}
        onKeyDown={this.onKeyDown}>
        {cells}
      </tr>
    );
  }
}
