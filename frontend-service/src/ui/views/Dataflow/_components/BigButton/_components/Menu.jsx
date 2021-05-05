import { Component } from 'react';
import styles from './Menu.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isNil } from 'lodash';

class Menu extends Component {
  constructor(props) {
    super(props);
    this.state = {
      style: {
        display: 'none'
      },
      menuClick: false
    };

    ['show', 'hide'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  hide() {
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
    // const menu = event.target.getBoundingClientRect();
    const menu = event.currentTarget.nextElementSibling;
    console.log('menu :>> ', menu);
    const button = event.currentTarget;
    const left = `${button.offsetLeft}px`;
    const topValue = button.offsetHeight + button.offsetTop + 3;
    const top = `${topValue}px `;
    // const top = `${button.offsetTop}px`;
    menu.style.left = left;
    menu.style.top = top;
    const menuLeft = left;
    const menuTop = top;
    // const buttonPosition = event.target.getBoundingClientRect();    
    
    this.setState(
      state => {
        return {
          ...state,
          style: {
            ...state.style,
            display: 'block'
          }
        };
      },
      () => {
        document.addEventListener('click', this.hide);
        this.setState(
          state => {
            return {
              ...state,
              style: {
                ...state.style,
                bottom: `-${menuTop}px`,
                left: `${menuLeft}px`,
                // left: `${buttonPosition.left}px`
              }
            };
          },
          
          () => {
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
            }, 50);
          }
        );
      }
    );
  }
  
  render() {
    const { model } = this.props;
    console.log('model :>> ', model);
    if (model) {
      return (
        <div className={`${styles.dropDownMenu} p-menu-overlay-visible p-menu`} style={this.state.style}>
          <ul className={'p-menu-list p-reset'}>
            {model ? (
              model.map((item, i) => (
                <li key={i} className={'p-menuitem'}>                 
                  {/* {item.items ? item.items.forEach(subItem => {
                    this.renderItem(subItem)
                  }) : this.renderItem(item)} */}
                  <a
                    className={`p-menuitem-link ${item.disabled ? styles.menuItemDisabled : null} ${isNil(item.icon) && styles.separator}`}
                    onClick={e => {
                      e.preventDefault();
                      if (!item.disabled) item.command();
                      else
                        this.setState(state => {
                          return { ...state, menuClick: true };
                        });
                    }}
                    disabled={item.disabled}>
                    {!isNil(item.icon) && <FontAwesomeIcon className={styles.userDataIcon} icon={AwesomeIcons(item.icon)}/>}
                    <span>{item.label}</span>
                  </a>                  
                </li>
              ))
            ) : (
              <li></li>
            )}
          </ul>
        </div>
      );
    } else {
      return <></>;
    }
  }
}

export { Menu };
