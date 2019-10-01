import React from 'react';
import { faColumns } from '@fortawesome/free-solid-svg-icons';

class VisibilityMenu extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      style: {
        display: 'none'
      }
    };
    this.show = this.show.bind(this);
  }
  show(event) {
    console.log(event);

    this.setState(state => {
      return {
        ...state,
        style: {
          ...state.style,
          display: state.style.display === 'none' ? 'block' : 'none'
        }
      };
    });
  }
  render() {
    const { columns } = this.props;
    return (
      <div style={this.state.style}>
        <ul>
          {columns.map((column, i) => (
            <li key={i}>{column.label}</li>
          ))}
        </ul>
      </div>
    );
  }
}

export { VisibilityMenu };
