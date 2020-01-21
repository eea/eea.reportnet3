import React from 'react';

import PropTypes from 'prop-types';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isUndefined from 'lodash/isUndefined';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './DropdownFilter.module.css';

class DropdownFilter extends React.Component {
  static defaultProps = {
    selectAll: false,
    showFilters: undefined,
    showNotCheckedFilters: undefined
  };

  static propTypes = {
    selectAll: PropTypes.bool,
    showFilters: PropTypes.func,
    showNotCheckedFilters: PropTypes.func
  };

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
    ['show', 'updateChecked', 'hide'].forEach(item => {
      this[item] = this[item].bind(this);
    });
  }

  componentDidUpdate(prevProps) {
    const { filters } = this.props;
    if (filters && prevProps.filters !== filters) {
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
    const { disabled } = this.props;
    if (disabled) {
      return;
    }

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
        if (!isUndefined(this.props.showFilters)) {
          this.props.showFilters(
            this.state.fields
              .filter(field => field.checked)
              .map(field => {
                return field.key;
              })
          );
        }
        if (!isUndefined(this.props.showNotCheckedFilters)) {
          this.props.showNotCheckedFilters(
            this.state.fields
              .filter(field => !field.checked)
              .map(field => {
                return field.label;
              })
          );
        }
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
            <li key={i} className={styles.selectNone}>
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
