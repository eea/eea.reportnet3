package org.eea.validation.util.datalake;

import org.apache.commons.lang3.StringUtils;
import org.eea.validation.util.ValidationDroolsUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DremioNonSQLValidationUtils {

    private static DremioNonSQLValidationUtils instance;

    public static synchronized DremioNonSQLValidationUtils getInstance() {
        if (instance == null) {
            instance = new DremioNonSQLValidationUtils();
        }
        return instance;
    }

    public boolean isNumberInteger(String value) {
        if(!isBlank(value)){
            return true;
        }
        try {
            Long.valueOf(value);
            return true;
        } catch( Exception e ){
            return false;
        }
    }

    public boolean isNumberDecimal(String value){
        if(!isBlank(value)){
            return true;
        }
        try {
            Double.valueOf( value );
            boolean numeric = true;
            numeric = value.matches("-?\\d+(\\.\\d+)?");
            if(numeric) {
                return true;
            }
            else {
                return false;
            }
        } catch( Exception e ){
            return false;
        }
    }

    public boolean isBlank(String value){
        return !StringUtils.isEmpty(value);
    }

   public boolean isDateYYYYMMDD(String value) {
        if (!isBlank(value)) {
            return true;
        }

       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       if(!isBlank(value)){
           return true;
       }
       LocalDate localDate = null;
       try {
           sdf.setLenient(false);
           localDate = LocalDate.parse(value);
       } catch( Exception e ){
           return false;
       }

       return true;
    }

   public boolean isDateTime(String value){
        if(!isBlank(value)){
            return true;
        }

        if(!value.matches(".+[zZ0-9]$")){
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(!isBlank(value)){
            return true;
        }
        Date dateDrools = null;
        try {
            sdf.setLenient(false);
            dateDrools = sdf.parse(value.replaceAll("[zZ]$", "").replace('T', ' '));
        } catch( Exception e ){
            return false;
        }
        return true;
    }

    public boolean isURL(String value){
        if(!isBlank(value)){
            return true;
        }
        return ValidationDroolsUtils.validateRegExpression(value,"REG_EXP_URL");
    }

    public boolean isEmail(String value){
        if(!isBlank(value)){
            return true;
        }
        return ValidationDroolsUtils.validateRegExpression(value,"REG_EXP_EMAIL");
    }

    public boolean isPhone(String value){
        if(!isBlank(value)){
            return true;
        }
        return ValidationDroolsUtils.validateRegExpression(value,"REG_EXP_PHONE");
    }

    public boolean isBlankPoint(byte[] byteArray) {
      // If there is no value at all, this rule should not apply.
      if (byteArray == null || byteArray.length == 0) {
        return true;
      }
      try {
        Geometry geometry = new WKBReader().read(byteArray);
        // Parameter exist but can't be converted to Geometry.
        if (geometry == null) {
          return false;
        }

        // Empty point (GeoJSON coordinates: []).
        if (geometry.isEmpty()) {
          return false;
        }

        // BlankPoint invoked but not POINT type.
        if (!"Point".equalsIgnoreCase(geometry.getGeometryType())) {
          return false;
        }

        Coordinate coordinates = geometry.getCoordinate();
        if (coordinates == null) {
          return false;
        }

        double x = coordinates.getX();
        double y = coordinates.getY();

        // Blank point [0,0] coordinates.
        return x != 0d || y != 0d;
      } catch (Exception e) {
        return true;
      }
    }
}















