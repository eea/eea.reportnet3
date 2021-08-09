import { Component } from 'react';
import style from './DropDownMenu.module.scss';
import { Icon } from 'views/_components/Icon';

class DropDownMenu extends Component {
  constructor(props) {
    super(props);
    this.state = {
      style: {
        display: 'none'
      },
      menuClick: false
    };

    ['show', 'hide'].map(item => (this[item] = this[item].bind(this)));
  }

  hide(event) {
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
    const menu = event.currentTarget.nextSibling;

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
                bottom: `-${menu.offsetHeight}px`
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
    if (model) {
      return (
        <div className={style.dropDownMenu} style={this.state.style}>
          <ul>
            {model ? (
              model.map((item, i) =>
                item.title ? (
                  <li className={style.listItemTitle} key={item.label}>
                    <div className={style.title}>
                      <Icon icon={item.icon} style={item.iconStyle ?? item.style} />
                      {item.label}
                    </div>
                  </li>
                ) : (
                  <li
                    className={item.disabled ? style.listItemDisabled : style.listItemEnabled}
                    key={item.label}
                    onClick={e => {
                      e.preventDefault();
                      if (!item.disabled) item.command();
                      else
                        this.setState(state => {
                          return { ...state, menuClick: true };
                        });
                    }}>
                    <div className={item.disabled ? style.menuItemDisabled : null} disabled={item.disabled}>
                      <Icon icon={item.icon} style={item.iconStyle ?? item.style} />
                      {item.label}
                    </div>
                  </li>
                )
              )
            ) : (
              <li></li>
            )}
          </ul>
        </div>
      );
    } else {
      return null;
    }
  }
}

export { DropDownMenu };
