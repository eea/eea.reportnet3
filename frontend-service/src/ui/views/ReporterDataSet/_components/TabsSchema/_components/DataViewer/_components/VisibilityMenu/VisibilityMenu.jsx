import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './VisibilityMenu.module.css';

class VisibilityMenu extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      style: {
        display: 'none'
      },
      htmlElement: document.getElementsByTagName('html')[0],
      fields: [],
      menuClick: false
    };
    ['show', 'updateChecked', 'hide'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  componentDidUpdate(prevProps) {
    const { columns } = this.props;
    if (prevProps.columns !== columns) {
      const fields = columns.map(column => ({
        checked: true,
        label: column.label,
        key: column.key
      }));
      this.setState(state => {
        return { ...state, fields: fields };
      });
    }
  }
  hide(e) {
    if (!this.state.menuClick) {
      this.setState(
        state => {
          return {
            ...state,
            style: {
              ...state.style,
              display: 'none'
            }
          };
        },
        () => {
          document.removeEventListener('click', this.hide, false);
        }
      );
    } else {
      this.setState(state => {
        return {
          ...state,
          menuClick: false
        };
      });
    }
  }

  show(event) {
    const { currentTarget } = event;
    const left = currentTarget.offsetLeft;

    this.setState(
      state => {
        return {
          ...state,
          style: {
            ...state.style,
            display: 'block',
            left: `${left}px`
          }
        };
      },
      () => {
        document.addEventListener('click', this.hide);
        setTimeout(() => {
          this.setState(state => {
            return {
              ...state,
              style: {
                ...state.style,
                opacity: 1
              }
            };
          });
        });
      }
    );
  }
  updateChecked(fieldKey) {
    let checked = null;
    const fields = this.state.fields.map(field => {
      if (field.key === fieldKey) {
        field.checked = !field.checked;
        checked = !field.checked;
      }
      return field;
    });
    if (checked) {
      this.props.hideColumn(fieldKey);
    } else {
      this.props.addColumn(fieldKey);
    }
    this.setState(state => {
      return {
        ...state,
        fields: fields
      };
    });
  }
  menuClick(e) {
    this.setState(state => {
      return {
        ...state,
        menuClick: true
      };
    });
  }
  render() {
    const { fields } = this.state;
    return (
      <div
        className={`${styles.visibilityMenu} p-menu-overlay-visible`}
        style={this.state.style}
        onClick={e => {
          this.menuClick(e);
        }}>
        <ul>
          {fields.map((field, i) => (
            <li key={i}>
              <a
                className={!field.checked ? styles.isNotChecked : ''}
                onClick={e => {
                  this.updateChecked(field.key);
                }}>
                <FontAwesomeIcon icon={field.checked ? AwesomeIcons('checkedSquare') : AwesomeIcons('square')} />
                {field.label}
              </a>
            </li>
          ))}
        </ul>
      </div>
    );
  }
}

export { VisibilityMenu };
