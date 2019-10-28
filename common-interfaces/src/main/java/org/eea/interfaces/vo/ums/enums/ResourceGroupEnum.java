package org.eea.interfaces.vo.ums.enums;


/**
 * The enum Resource group enum.
 */
public enum ResourceGroupEnum {
  /**
   * Dataflow provider resource group enum.
   */
  DATAFLOW_PROVIDER("Dataflow-%s-DATA_PROVIDER"),
  /**
   * Dataflow requester resource group enum.
   */
  DATAFLOW_REQUESTER("Dataflow-%s-DATA_REQUESTER"),
  /**
   * Dataflow steward resource group enum.
   */
  DATAFLOW_STEWARD("Dataflow-%s-DATA_STEWARD"),
  /**
   * Dataflow custodian resource group enum.
   */
  DATAFLOW_CUSTODIAN("Dataflow-%s-DATA_CUSTODIAN"),
  /**
   * Dataset provider resource group enum.
   */
  DATASET_PROVIDER("Dataset-%s-DATA_PROVIDER"),
  /**
   * Dataset requester resource group enum.
   */
  DATASET_REQUESTER("Dataset-%s-DATA_REQUESTER"),
  /**
   * Dataset steward resource group enum.
   */
  DATASET_STEWARD("Dataset-%s-DATA_STEWARD"),
  /**
   * Dataset custodian resource group enum.
   */
  DATASET_CUSTODIAN("Dataset-%s-DATA_CUSTODIAN"),

  /**
   * Dataschema custodian resource group enum.
   */
  DATASCHEMA_CUSTODIAN("Dataschema-%s-DATA_CUSTODIAN"),
  /**
   * Dataschema requester resource group enum.
   */
  DATASCHEMA_REQUESTER("Dataschema-%s-DATA_REQUESTER"),
  /**
   * Dataschema provider resource group enum.
   */
  DATASCHEMA_PROVIDER("Dataschema-%s-DATA_PROVIDER");


  private String expression;

  private ResourceGroupEnum(String expression) {
    this.expression = expression;
  }


  /**
   * Gets group name.
   *
   * @param idEntity the id entity
   *
   * @return the group name
   */
  public String getGroupName(Long idEntity) {
    return String.format(this.expression, idEntity);
  }

  /**
   * From resource type and security role resource group enum.
   *
   * @param resourceTypeEnum the resource type enum
   * @param securityRoleEnum the security role enum
   *
   * @return the resource group enum
   */
  public static ResourceGroupEnum fromResourceTypeAndSecurityRole(ResourceTypeEnum resourceTypeEnum,
      SecurityRoleEnum securityRoleEnum) {
    String resource = resourceTypeEnum.toString();
    String role = securityRoleEnum.toString();
    String resourceGroupExpresion = new StringBuilder(resource).append("-%s-").append(role)
        .toString();
    ResourceGroupEnum[] values = ResourceGroupEnum.values();
    ResourceGroupEnum result = null;
    for (ResourceGroupEnum value : values) {
      if (resourceGroupExpresion.equals(value.expression)) {
        result = value;
        break;
      }
    }
    return result;
  }
}
