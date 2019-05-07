package org.eea.validation.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.model.Rules;
import org.eea.validation.repository.RulesRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.google.common.collect.Lists;

@Configuration
@EnableJpaRepositories(basePackages = "org.eea.validation.repository")
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.validation.model")
public class ValidationConfiguration {

  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";

  @Autowired
  private RulesRepository rulesRepository;

  @Bean
  public KieBase kieBase() throws IOException {

    Date init = new Date();
    Iterable<Rules> preRepositoryDB = rulesRepository.findAll();
    Date end = new Date();
    System.out.println(
        String.format("Tiempo total %s segundos", (end.getTime() - init.getTime()) / 1000));
    List<Rules> preRepository = Lists.newArrayList(preRepositoryDB);

    List<Map<String, String>> ruleAttributes = new ArrayList<>();

    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getName());
      rule1.put("whencondition",
          preRepository.get(i).getAttribute() + preRepository.get(i).getConditionalElement());
      rule1.put("thencondition", preRepository.get(i).getAction());
      ruleAttributes.add(rule1);
    }

    ObjectDataCompiler compiler = new ObjectDataCompiler();

    String generatedDRL =
        compiler.compile(ruleAttributes, new FileInputStream(REGULATION_TEMPLATE_FILE));

    KieServices kieServices = KieServices.Factory.get();

    KieHelper kieHelper = new KieHelper();

    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);

    KieBase kieBase = kieHelper.build();

    return kieBase;
  }
}
