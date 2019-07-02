package org.eea.interfaces.vo.dataflow.enums;

public enum TypeRequestEnum {

  MANDATORY("MANDATORY"),

  OPTIONAL("OPTIONAL");

  private final String value;


  TypeRequestEnum(String value) {
    this.value = value;
  }


  public String getValue() {
    return value;
  }


}
