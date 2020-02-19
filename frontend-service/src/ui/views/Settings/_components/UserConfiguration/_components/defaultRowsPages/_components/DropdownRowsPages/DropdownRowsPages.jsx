import React, { Component } from 'react';
import { Dropdown } from 'primereact/dropdown';
export class DropdownRowsPages extends Component {
  static defaultProps = { placeholder: null };
  constructor() {
    super();
    this.state = {
      city: null,
      car: null,
      car2: 'BMW'
    };

    this.onCityChange = this.onCityChange.bind(this);
  }

  onCityChange(e) {
    this.setState({ city: e.value });
  }
  carTemplate(option) {
    if (!option.value) {
      return option.label;
    } else {
      var logoPath = 'showcase/resources/demo/images/car/' + option.label + '.png';

      return (
        <div className="p-clearfix">
          <img
            alt={option.label}
            src={logoPath}
            style={{ display: 'inline-block', margin: '5px 0 0 5px' }}
            width="24"
          />
          <span style={{ float: 'right', margin: '.5em .25em 0 0' }}>{option.label}</span>
        </div>
      );
    }
  }

  render() {
    const cities = [{ row: 5 }, { row: 10 }, { row: 20 }, { row: 100 }];

    return (
      <div>
        <div className="content-section introduction">
          <div className="feature-intro"></div>
        </div>

        <div className="content-section implementation">
          <Dropdown
            value={this.state.city}
            options={cities}
            onChange={this.onCityChange}
            placeholder={this.props.placeholder}
            optionLabel="row"
          />
        </div>
      </div>
    );
  }
}
