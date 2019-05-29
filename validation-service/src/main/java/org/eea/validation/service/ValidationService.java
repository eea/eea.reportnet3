package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import org.eea.validation.model.rules.Rule;
import org.eea.validation.repository.DataSetSchemaRepository;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
public class ValidationService {


  /**
   * Gets the element lenght.
   *
   * @return the element lenght
   */
  public void getElementLenght() {
    /*
     *  to be done in sprint 2
     */
  }

  /**
   * Gets the rules.
   *
   * @param rules the rules
   * @return the rules
   */
  public List<Map<String, String>> getRules(Rule rules) {
    return null;
  }

  /**
   * Sets the new rules.
   *
   * @param newRules the new new rules
   */
  public void setNewRules(Rule newRules) {
    /*
     *  to be done in sprint 2
     */
  }

  // Object convertToObjectId(Object id) {
  // if (id instanceof String && ObjectId.isValid(id)) {
  // return new ObjectId(id);
  // }
  // return id;
  /**
   * Load new rules.
   *
   * @param rules the rules
   * @return the kie base
   */
  // }
  public KieBase loadNewRules(Rule rules) {
    return null;
  }

}
