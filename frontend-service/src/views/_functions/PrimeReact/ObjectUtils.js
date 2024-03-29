import { TextUtils } from 'repositories/_utils/TextUtils';

export default class ObjectUtils {
  static equals(obj1, obj2, field) {
    if (field) return this.resolveFieldData(obj1, field) === this.resolveFieldData(obj2, field);
    else return this.deepEquals(obj1, obj2);
  }

  static deepEquals(a, b) {
    if (a === b) return true;

    if (a && b && typeof a == 'object' && typeof b == 'object') {
      var arrA = Array.isArray(a),
        arrB = Array.isArray(b),
        i,
        length,
        key;

      if (arrA && arrB) {
        length = a.length;
        if (length !== b.length) return false;
        for (i = length; i-- !== 0; ) if (!this.deepEquals(a[i], b[i])) return false;
        return true;
      }

      if (arrA !== arrB) return false;

      var dateA = a instanceof Date,
        dateB = b instanceof Date;
      if (dateA !== dateB) return false;
      if (dateA && dateB) return a.getTime() === b.getTime();

      var regexpA = a instanceof RegExp,
        regexpB = b instanceof RegExp;
      if (regexpA !== regexpB) return false;
      if (regexpA && regexpB) return a.toString() === b.toString();

      var keys = Object.keys(a);
      length = keys.length;

      if (length !== Object.keys(b).length) return false;

      for (i = length; i-- !== 0; ) if (!Object.prototype.hasOwnProperty.call(b, keys[i])) return false;

      for (i = length; i-- !== 0; ) {
        key = keys[i];
        if (!this.deepEquals(a[key], b[key])) return false;
      }

      return true;
    }

    /*eslint no-self-compare: "off"*/
    return a !== a && b !== b;
  }

  static resolveFieldData(data, field) {
    if (data && field) {
      if (this.isFunction(field)) {
        return field(data);
      } else if (field.indexOf('.') === -1) {
        return data[field];
      } else {
        let fields = field.split('.');
        let value = data;
        for (var i = 0, len = fields.length; i < len; ++i) {
          if (value == null) {
            return null;
          }
          value = value[fields[i]];
        }
        return value;
      }
    } else {
      return null;
    }
  }

  static isFunction(obj) {
    return !!(obj && obj.constructor && obj.call && obj.apply);
  }

  static findDiffKeys(obj1, obj2) {
    if (!obj1 || !obj2) {
      return {};
    }

    return Object.keys(obj1)
      .filter(key => !obj2.hasOwnProperty(key))
      .reduce((result, current) => {
        result[current] = obj1[current];
        return result;
      }, {});
  }

  static filter(value, fields, filterValue) {
    var filteredItems = [];

    if (value) {
      for (let item of value) {
        for (let field of fields) {
          if (String(this.resolveFieldData(item, field)).toLowerCase().indexOf(filterValue.toLowerCase()) > -1) {
            filteredItems.push(item);
            break;
          }
        }
      }
    }

    return filteredItems;
  }

  static reorderArray(value, from, to) {
    let target;
    if (value && from !== to) {
      if (to >= value.length) {
        target = to - value.length;
        while (target-- + 1) {
          value.push(undefined);
        }
      }
      value.splice(to, 0, value.splice(from, 1)[0]);
    }
  }

  static findIndexInList(value, list) {
    let index = -1;

    if (list) {
      for (let i = 0; i < list.length; i++) {
        if (list[i] === value) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

  static getJSXElement(obj, ...params) {
    return this.isFunction(obj) ? obj(...params) : obj;
  }

  static filterConstraints = {
    startsWith(value, filter) {
      if (filter === undefined || filter === null || filter.trim() === '') {
        return true;
      }

      if (value === undefined || value === null) {
        return false;
      }

      let filterValue = filter.toLowerCase();
      return value.toString().toLowerCase().slice(0, filterValue.length) === filterValue;
    },

    contains(value, filter) {
      if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
        return true;
      }

      if (value === undefined || value === null) {
        return false;
      }

      return value.toString().toLowerCase().indexOf(filter.toLowerCase()) !== -1;
    },

    endsWith(value, filter) {
      if (filter === undefined || filter === null || filter.trim() === '') {
        return true;
      }

      if (value === undefined || value === null) {
        return false;
      }

      let filterValue = filter.toString().toLowerCase();
      return (
        value
          .toString()
          .toLowerCase()
          .indexOf(filterValue, value.toString().length - filterValue.length) !== -1
      );
    },

    equals(value, filter) {
      if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
        return true;
      }

      if (value === undefined || value === null) {
        return false;
      }

      return TextUtils.areEquals(value.toString(), filter.toString());
    },

    notEquals(value, filter) {
      if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
        return false;
      }

      if (value === undefined || value === null) {
        return true;
      }

      return !TextUtils.areEquals(value.toString(), filter.toString());
    },

    in(value, filter) {
      if (filter === undefined || filter === null || filter.length === 0) {
        return true;
      }

      if (value === undefined || value === null) {
        return false;
      }

      for (let i = 0; i < filter.length; i++) {
        if (filter[i] === value) return true;
      }

      return false;
    }
  };

  static removeAccents(str) {
    if (str && str.search(/[\xC0-\xFF]/g) > -1) {
      str = str
        .replace(/[\xC0-\xC5]/g, 'A')
        .replace(/[\xC6]/g, 'AE')
        .replace(/[\xC7]/g, 'C')
        .replace(/[\xC8-\xCB]/g, 'E')
        .replace(/[\xCC-\xCF]/g, 'I')
        .replace(/[\xD0]/g, 'D')
        .replace(/[\xD1]/g, 'N')
        .replace(/[\xD2-\xD6\xD8]/g, 'O')
        .replace(/[\xD9-\xDC]/g, 'U')
        .replace(/[\xDD]/g, 'Y')
        .replace(/[\xDE]/g, 'P')
        .replace(/[\xE0-\xE5]/g, 'a')
        .replace(/[\xE6]/g, 'ae')
        .replace(/[\xE7]/g, 'c')
        .replace(/[\xE8-\xEB]/g, 'e')
        .replace(/[\xEC-\xEF]/g, 'i')
        .replace(/[\xF1]/g, 'n')
        .replace(/[\xF2-\xF6\xF8]/g, 'o')
        .replace(/[\xF9-\xFC]/g, 'u')
        .replace(/[\xFE]/g, 'p')
        .replace(/[\xFD\xFF]/g, 'y');
    }

    return str;
  }
}
