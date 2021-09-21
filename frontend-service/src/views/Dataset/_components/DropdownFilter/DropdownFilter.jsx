import { Component } from 'react';
import PropTypes from 'prop-types';

import isUndefined from 'lodash/isUndefined';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconTooltip } from 'views/_components/IconTooltip';
import { LevelError } from 'views/_components/LevelError';

import styles from './DropdownFilter.module.scss';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

class DropdownFilter extends Component {
  static defaultProps = {
    showFilters: undefined,
    showLevelErrorIcons: false,
    showNotCheckedFilters: undefined
  };

  static propTypes = {
    showFilters: PropTypes.func,
    showLevelErrorIcons: PropTypes.bool,
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
      let fields = filters.map(column => ({
        checked: true,
        label: column.label,
        key: column.key
      }));

      const selectAllField = {
        checked: true,
        label: this.context.messages['checkAllFilter'],
        key: 'selectAll'
      };

      fields.unshift(selectAllField);

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
    if (this.props.hide) {
      this.props.hide();
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

    let newFields;

    if (disabled) {
      return;
    }

    if (fieldKey === 'selectAll') {
      const selectAllField = fields.find(field => field.key === fieldKey);

      if (selectAllField) {
        newFields = fields.map(field => {
          if (field.key === 'selectAll') {
            field.checked = !field.checked;
          } else {
            field.checked = selectAllField.checked;
          }

          return field;
        });
      }
    } else {
      newFields = fields.map(field => {
        if (field.key === fieldKey) {
          field.checked = !field.checked;
        }

        return field;
      });

      newFields = this.controlSelectAllChecked(newFields);
    }

    this.setState(
      state => {
        return {
          ...state,
          fields: newFields
        };
      },
      () => {
        if (!isUndefined(this.props.showFilters)) {
          this.props.showFilters(this.filterOutCheckedFields());
        }

        if (!isUndefined(this.props.showNotCheckedFilters)) {
          this.props.showNotCheckedFilters(this.filterUncheckedFields());
        }
      }
    );
  }

  filterUncheckedFields() {
    return this.state.fields.filter(field => !field.checked).map(field => field.key);
  }

  filterOutCheckedFields() {
    return this.state.fields.filter(field => field.checked).map(field => field.key);
  }

  hasAnyUncheckedField(filters) {
    let result = false;

    for (let index = 0; index < filters.length; index++) {
      const filter = filters[index];

      if (filter.key !== 'selectAll' && filter.checked) {
        result = true;
        break;
      }
    }

    return result;
  }

  controlSelectAllChecked(newFields) {
    if (this.hasAnyUncheckedField(newFields)) {
      newFields = newFields.map(field => {
        if (field.key !== 'selectAll') {
          return field;
        } else {
          field.checked = false;
          return field;
        }
      });
    }

    const whenSelectAllIsTheOnlyOneUnchecked = fields => {
      let isSelectAllChecked = false;
      let isAnyOtherFieldUnchecked = false;

      fields.forEach(field => {
        if (field.key !== 'selectAll') {
          if (field.checked === false) {
            isAnyOtherFieldUnchecked = true;
          }
        } else {
          isSelectAllChecked = field.checked;
        }
      });

      if (isSelectAllChecked === false && isAnyOtherFieldUnchecked === false) {
        newFields = fields.map(field => {
          if (field.key === 'selectAll') {
            field.checked = true;
          }
          return field;
        });
      } else if (isAnyOtherFieldUnchecked === true && isSelectAllChecked === true) {
        newFields = fields.map(field => {
          if (field.key === 'selectAll') {
            field.checked = false;
          }
          return field;
        });
      }
    };

    whenSelectAllIsTheOnlyOneUnchecked(newFields);

    return newFields;
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
        onClick={e => {
          this.menuClick(e);
        }}
        style={this.state.style}>
        <ul>
          {fields.map(field => (
            <li className={styles.selectNone} key={`parent_${field.key}`}>
              <div
                className={!field.checked ? styles.isNotChecked : ''}
                onClick={() => {
                  this.updateChecked(field.key);
                }}>
                <FontAwesomeIcon
                  aria-label={field.label.toLowerCase()}
                  className={styles.checkboxIcon}
                  icon={field.checked ? AwesomeIcons('checkedSquare') : AwesomeIcons('square')}
                  role="presentation"
                />
                {this.props.showLevelErrorIcons ? (
                  !TextUtils.areEquals(field.label, 'SELECT ALL') && !TextUtils.areEquals(field.label, 'CORRECT') ? (
                    <IconTooltip
                      className={styles.dropdownFilterIcon}
                      key={field.label.toUpperCase()}
                      levelError={field.label.toUpperCase()}
                      message={''}
                      style={{ width: '1.5em', height: '29px', verticalAlign: 'baseline', marginRight: '0.5em' }}
                    />
                  ) : (
                    <span
                      className={styles.dropdownFilterIcon}
                      style={{
                        display: 'inline-block',
                        height: '29px',
                        marginTop: '-0.4em',
                        verticalAlign: 'baseline',
                        width: '1.5em'
                      }}></span>
                  )
                ) : null}
                <LevelError type={field.label.toLowerCase()} />
              </div>
            </li>
          ))}
        </ul>
      </div>
    );
  }
}
DropdownFilter.contextType = ResourcesContext;

export { DropdownFilter };
