export class Country {
  constructor({ memberCountry, name, spatialId, twoLetter, type } = {}) {
    this.memberCountry = memberCountry;
    this.name = name;
    this.spatialId = spatialId;
    this.twoLetter = twoLetter;
    this.type = type;
  }
}
