import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isEqual } from 'lodash';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './DropdownFilter.module.css';

class DropdownFilter extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      style: {
        display: 'none'
      },
      fields: [],
      menuClick: false
    };
    ['show', 'updateChecked', 'hide'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  componentDidUpdate(prevProps) {
    const { filters } = this.props;
    console.log('componentDidUpdate', filters, prevProps.filters);

    if (prevProps.filters !== filters)) {
      console.log('no equals');

      const fields = filters.map(column => ({
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
    const { fields } = this.state;
    const newFields = fields.map(field => {
      if (field.key === fieldKey) {
        field.checked = !field.checked;
      }
      return field;
    });
    this.setState(
      state => {
        return {
          ...state,
          fields: newFields
        };
      },
      () => {
        this.props.showFilters(
          this.state.fields
            .filter(field => field.checked)
            .map(field => {
              return field.key;
            })
        );
      }
    );
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
        className={`${styles.dropdownFilter} p-menu-overlay-visible`}
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

export { DropdownFilter };
