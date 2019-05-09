package org.eea.validation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "RULES_DROOLS_BASE")
public class Rules {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID_RULE")
  private Integer idRule;

  @Column(name = "KIE_BASE")
  private String rulesBase;

  @Column(name = "KIE_SESSION")
  private String rulesSession;

  @Column(name = "KIE_AGENT")
  private String rulesAgent;

  @Column(name = "RULE_NAME")
  private String name;

  @Column(name = "RULE_ATTRIBUTE")
  private String attribute;

  @Column(name = "RULE_CONDITION")
  private String conditionalElement;

  @Column(name = "RULE_ACTION")
  private String action;

  /**
   * @return the idRule
   */
  public Integer getIdRule() {
    return idRule;
  }

  /**
   * @param idRule the idRule to set
   */
  public void setIdRule(Integer idRule) {
    this.idRule = idRule;
  }

  /**
   * @return the rulesBase
   */
  public String getRulesBase() {
    return rulesBase;
  }

  /**
   * @param rulesBase the rulesBase to set
   */
  public void setRulesBase(String rulesBase) {
    this.rulesBase = rulesBase;
  }

  /**
   * @return the rulesSession
   */
  public String getRulesSession() {
    return rulesSession;
  }

  /**
   * @param rulesSession the rulesSession to set
   */
  public void setRulesSession(String rulesSession) {
    this.rulesSession = rulesSession;
  }

  /**
   * @return the rulesAgent
   */
  public String getRulesAgent() {
    return rulesAgent;
  }

  /**
   * @param rulesAgent the rulesAgent to set
   */
  public void setRulesAgent(String rulesAgent) {
    this.rulesAgent = rulesAgent;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the attribute
   */
  public String getAttribute() {
    return attribute;
  }

  /**
   * @param attribute the attribute to set
   */
  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  /**
   * @return the conditionalElement
   */
  public String getConditionalElement() {
    return conditionalElement;
  }

  /**
   * @param conditionalElement the conditionalElement to set
   */
  public void setConditionalElement(String conditionalElement) {
    this.conditionalElement = conditionalElement;
  }

  /**
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }


}
