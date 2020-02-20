import React, { Component } from 'react';
import { Dropdown } from 'primereact/dropdown';
export class DropdownRowsPages extends Component {
  static defaultProps = { placeholder: null };
  constructor() {
    super();
    this.state = {
      row: null
    };

    this.onCityChange = this.onCityChange.bind(this);
  }

  onCityChange(e) {
    this.setState({ row: e.value });
  }

  render() {
    const rows = [{ row: 5 }, { row: 10 }, { row: 20 }, { row: 100 }];

    return (
      <div>
        <div className="content-section introduction">
          <div className="feature-intro"></div>
        </div>

        <div className="content-section implementation">
          <Dropdown
            value={this.state.row}
            options={rows}
            onChange={this.onCityChange}
            placeholder={this.props.placeholder}
            optionLabel="row"
          />
        </div>
      </div>
    );
  }
}
