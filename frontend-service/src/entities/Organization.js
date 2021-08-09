export class Organization {
  constructor({ acronym, address, city, country, description, email, id, name, postalCode, shortName, url } = {}) {
    this.acronym = acronym;
    this.address = address;
    this.city = city;
    this.country = country;
    this.description = description;
    this.email = email;
    this.id = id;
    this.name = name;
    this.postalCode = postalCode;
    this.shortName = shortName;
    this.url = url;
  }
}
