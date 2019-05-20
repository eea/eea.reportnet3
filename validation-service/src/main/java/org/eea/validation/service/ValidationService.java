package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.validation.model.DataSetSchema;
import org.eea.validation.model.rules.Rule;
import org.eea.validation.repository.DataSetSchemaRepository;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {


  @Autowired
  private DataSetSchemaRepository dataSetSchemaRepository;

  public void getElementLenght() {


  }

  public List<Map<String, String>> getRules(Rule rules) {
    return null;
  }

  public void setNewRules(Rule newRules) {

    // List<TableSchema> tableSchemaList = new ArrayList();
    // TableSchema esquema = new TableSchema();
    // esquema.setIdTableSchema(new ObjectId());
    // esquema.setNameSchema("nombre probando123");
    //
    // RecordSchema recordSchema = new RecordSchema();
    // recordSchema.setNameSchema("NAME RECORD SCHEMA");
    // recordSchema.setIdRecordSchema(new ObjectId());
    //
    //
    // DataSetSchema dataSetSchema = new DataSetSchema();
    //
    // List<FieldSchema> fieldSchemaList = new ArrayList();
    // FieldSchema fieldSchema1 = new FieldSchema();
    // fieldSchema1.setIdFieldSchema(new ObjectId());
    // fieldSchema1.setType(HeaderType.BOOLEAN);
    // FieldSchema fieldSchema2 = new FieldSchema();
    // fieldSchema2.setType(HeaderType.STRING);
    // fieldSchema2.setIdFieldSchema(new ObjectId());
    // fieldSchemaList.add(fieldSchema1);
    // fieldSchemaList.add(fieldSchema2);
    //
    // recordSchema.setFieldSchema(fieldSchemaList);
    // esquema.setRecordSchema(recordSchema);
    // esquema.setTableRuleList(new ArrayList());
    // tableSchemaList.add(esquema);
    // dataSetSchema.setIdDataSetSchema(new ObjectId());
    // dataSetSchema.setTableSchemas(tableSchemaList);
    // DataSetSchema test = dataSetSchemaRepository.save(dataSetSchema);
    // esquema.setNameSchema("nombre probando1234");
    // recordSchema.setNameSchema("NAME RECORD SCHEMA 2");
    // esquema.setRecordSchema(recordSchema);
    // dataSetSchema.setIdDataSetSchema(new ObjectId());
    // dataSetSchemaRepository.save(dataSetSchema);
    List<DataSetSchema> listaTabla = dataSetSchemaRepository.findAll();;
    Optional<DataSetSchema> dato =
        dataSetSchemaRepository.findById(new ObjectId("34234234234234234234"));
    DataSetSchema pasar = dato.get();
    // var objectId = mongoose.Types.ObjectId('569ed8269353e9f4c51617aa');
    dataSetSchemaRepository.deleteByTableSchemasNameSchema("nombre probando123");
    // rulesRepository.save(newRules);

  }

  // Object convertToObjectId(Object id) {
  // if (id instanceof String && ObjectId.isValid(id)) {
  // return new ObjectId(id);
  // }
  // return id;
  // }
  public KieBase loadNewRules(Rule rules) {
    return null;
  }

}
