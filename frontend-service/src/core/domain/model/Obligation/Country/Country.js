export class Country {
  constructor({ countryCode, countryMember, dataflows, id, name, type } = {}) {
    this.countryCode = countryCode;
    this.countryMember = countryMember;
    this.dataflows = dataflows;
    this.id = id;
    this.name = name;
    this.type = type;
  }
}
