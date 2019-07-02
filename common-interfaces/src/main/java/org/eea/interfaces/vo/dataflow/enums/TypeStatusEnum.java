package org.eea.interfaces.vo.dataflow.enums;

public enum TypeStatusEnum {


  PENDING("PENDING"),


  ACCEPTED("ACCEPTED"),


  COMPLETED("COMPLETED");


  private final String value;


  TypeStatusEnum(String value) {
    this.value = value;
  }


  public String getValue() {
    return value;
  }


}
