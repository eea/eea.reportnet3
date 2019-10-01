import React from 'react';
import styles from './VisibilityMenu.module.css';

class VisibilityMenu extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      style: {
        display: 'none'
      }
    };
    ['show'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  show(event) {
    const htmlElement = document.getElementsByTagName('html');

    this.setState(state => {
      return {
        ...state,
        style: {
          ...state.style,
          display: state.style.display === 'none' ? 'block' : 'none'
        },
        listener:
          state.style.display === 'none'
            ? htmlElement[0].addEventListener('click', e => {
                this.show(e);
              })
            : htmlElement.removeEventListener('click', this.state.listener, false)
      };
    });
  }
  render() {
    const { columns } = this.props;
    return (
      <div className={styles.visibilityMenu} style={this.state.style}>
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
