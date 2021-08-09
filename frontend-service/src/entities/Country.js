export class Country {
  constructor({ countryCode, countryMember, id, name, type } = {}) {
    this.countryCode = countryCode;
    this.countryMember = countryMember;
    this.id = id;
    this.name = name;
    this.type = type;
  }
}
