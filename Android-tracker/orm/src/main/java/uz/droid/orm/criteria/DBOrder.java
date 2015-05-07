package uz.droid.orm.criteria;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 24.10.13 15:00.
 * Database order
 */
public class DBOrder {

    public static final int ASC = 101;
    public static final int DESC = 102;

    private String fieldName;
    private int type;

    private DBOrder(String fieldName, int type) {
        this.fieldName = fieldName;
        this.type = type;
    }

    public static DBOrder asc(String fieldName) {
        return new DBOrder(fieldName, ASC);
    }

    public static DBOrder desc(String fieldName) {
        return new DBOrder(fieldName, DESC);
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getType() {
        return type;
    }
}
