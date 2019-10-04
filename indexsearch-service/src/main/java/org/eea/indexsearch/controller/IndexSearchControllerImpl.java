package org.eea.indexsearch.controller;


import java.util.List;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;
import org.eea.indexsearch.service.IndexSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/index")
public class IndexSearchControllerImpl<Employee> implements IndexSearchControler {


  @Autowired
  private IndexSearchService service;

  // @Override
  // @PostMapping
  // public ResponseEntity<String> createProfile(@RequestBody Employee employee) throws Exception {
  //
  //
  // return new ResponseEntity<String>(service.createProfile(employee), HttpStatus.CREATED);
  // }
  //
  // @Override
  // @PutMapping
  // public ResponseEntity<String> updateProfile(@RequestBody Employee employee) throws Exception {
  //
  // return new ResponseEntity<String>(service.updateProfile(employee), HttpStatus.CREATED);
  // }
  //
  // @Override
  // @GetMapping("/{id}")
  // public Employee findById(@PathVariable String id) throws Exception {
  //
  // return service.findById(id);
  // }
  //
  @Override
  @GetMapping
  public List<ElasticSearchData> findAll() throws Exception {

    return service.findAll();
  }

  //
  // @Override
  // @GetMapping(value = "/search")
  // public List<Employee> search(@RequestParam(value = "technology") String technology)
  // throws Exception {
  // return service.searchByTechnology(technology);
  // }
  //
  //
  // @Override
  // @GetMapping(value = "/name-search")
  // public List<Employee> searchByName(@RequestParam(value = "name") String name) throws Exception
  // {
  // return service.findProfileByName(name);
  // }
  //
  //
  //
  @DeleteMapping("/{id}")
  public String deleteProfileDocument(@PathVariable String id) throws Exception {

    return service.deleteProfileDocument(id);

  }

  @Override
  @GetMapping("/macros")
  public void executeMacros() throws Exception {

    // return service.executeMacros();

  }
}
