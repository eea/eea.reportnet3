import DomHandler from './DomHandler';

export default class KeyFilter {
  /* eslint-disable */
  static DEFAULT_MASKS = {
    alpha: /[a-z_]/i,
    alphanum: /[a-z0-9_]/i,
    any: /[\s\S]*/,
    coordinates: /[\d\.\s,.-]/,
    ///^(-?\d+(\.\d+)?),\s*(-?\d+(\.\d+)?)$/,
    date: /[\d\-]/i,
    email: /[a-z0-9_\.\-@]/i,
    hex: /[0-9a-f]/i,
    int: /[\d\-]/,
    money: /[\d\.\s,]/,
    noComma: /[^,]+/,
    noDoubleQuote: /[^"]+/,
    noSemicolon: /[^;]+/,
    num: /[\d\-\.]/,
    phone: /^(\(?\+?[0-9]*\)?)?[0-9_\- \(\)]*$/,
    pint: /[\d]/,
    pnum: /[\d\.]/,
    schemaTableFields: /[a-zA-Z0-9_\s\-]/,
    url: /^\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/
  };
  /* eslint-enable */

  static KEYS = {
    TAB: 9,
    RETURN: 13,
    ESC: 27,
    BACKSPACE: 8,
    DELETE: 46
  };

  static SAFARI_KEYS = {
    63234: 37, // left
    63235: 39, // right
    63232: 38, // up
    63233: 40, // down
    63276: 33, // page up
    63277: 34, // page down
    63272: 46, // delete
    63273: 36, // home
    63275: 35 // end
  };

  static isNavKeyPress(e) {
    let k = e.keyCode;
    k = DomHandler.getBrowser().safari ? KeyFilter.SAFARI_KEYS[k] || k : k;

    return (k >= 33 && k <= 40) || k === KeyFilter.KEYS.RETURN || k === KeyFilter.KEYS.TAB || k === KeyFilter.KEYS.ESC;
  }

  static isSpecialKey(e) {
    let k = e.keyCode;

    return (
      k === 9 ||
      k === 13 ||
      k === 27 ||
      k === 16 ||
      k === 17 ||
      (k >= 18 && k <= 20) ||
      (DomHandler.getBrowser().opera &&
        !e.shiftKey &&
        (k === 8 || (k >= 33 && k <= 35) || (k >= 36 && k <= 39) || (k >= 44 && k <= 45)))
    );
  }

  static getKey(e) {
    let k = e.keyCode || e.charCode;
    return DomHandler.getBrowser().safari ? KeyFilter.SAFARI_KEYS[k] || k : k;
  }

  static getCharCode(e) {
    return e.charCode || e.keyCode || e.which;
  }

  static onKeyPress(e, keyfilter, validateOnly) {
    if (validateOnly) {
      return;
    }

    const regex = KeyFilter.DEFAULT_MASKS[keyfilter] ? KeyFilter.DEFAULT_MASKS[keyfilter] : keyfilter;
    const browser = DomHandler.getBrowser();

    if (e.ctrlKey || e.altKey) {
      return;
    }

    const k = this.getKey(e);
    if (
      browser.mozilla &&
      (this.isNavKeyPress(e) || k === KeyFilter.KEYS.BACKSPACE || (k === KeyFilter.KEYS.DELETE && e.charCode === 0))
    ) {
      return;
    }

    const c = this.getCharCode(e);
    const cc = String.fromCharCode(c);

    if (browser.mozilla && (this.isSpecialKey(e) || !cc)) {
      return;
    }

    if (!regex.test(cc)) {
      e.preventDefault();
    }
  }

  static validate(e, keyfilter) {
    let value = e.target.value,
      validatePattern = true;

    if (value && !keyfilter.test(value)) {
      validatePattern = false;
    }

    return validatePattern;
  }
}
