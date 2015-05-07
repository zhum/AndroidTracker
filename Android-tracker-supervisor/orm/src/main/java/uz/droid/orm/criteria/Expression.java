package uz.droid.orm.criteria;


import java.io.Serializable;
import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 24.10.13 11:29.
 */
public class Expression {

    public static final int EQUALS = 101;
    public static final int GREATE = 102;
    public static final int GREATE_OR_EQUALS = 103;
    public static final int LESS = 104;
    public static final int LESS_OR_EQUALS = 105;
    public static final int OR = 106;
    public static final int NOT_EQUALS = 107;
    public static final int NOT_NULL = 108;
    public static final int IN = 109;
    public static final int NOT_IN = 110;

    private int type;
    private Object obj;
    private String fieldName;
    private Expression[] expressions;


    private Expression(int type, Object obj, String fieldName) {
        this.type = type;
        this.obj = obj;
        this.fieldName = fieldName;
    }

    private Expression(int type, Expression[] exps) {
        this.type = type;
        this.expressions = exps;
    }

    public static Expression eq(String fieldName, Object val) {
        return new Expression(EQUALS, val, fieldName);
    }

    public static Expression greate(String fieldName, Object val) {
        return new Expression(GREATE, val, fieldName);
    }

    public static Expression greateOrEq(String fieldName, Object val) {
        return new Expression(GREATE_OR_EQUALS, val, fieldName);
    }

    public static Expression less(String fieldName, Object val) {
        return new Expression(LESS, val, fieldName);
    }

    public static Expression lessOrEq(String fieldName, Object val) {
        return new Expression(LESS_OR_EQUALS, val, fieldName);
    }

    public static Expression or(Expression... exps) {
        return new Expression(OR, exps);
    }

    public static Expression notEq(String fieldName, Object val) {
        return new Expression(NOT_EQUALS, val, fieldName);
    }

    public static Expression notNull(String fieldName) {
        return new Expression(NOT_NULL, null, fieldName);
    }

    public static Expression in(String fieldName, List<? extends Serializable> values) {
        return new Expression(IN, values, fieldName);
    }

    public static Expression notIn(String fieldName, List<? extends Serializable> ids) {
        return new Expression(NOT_IN, ids, fieldName);
    }

    public int getType() {
        return type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getObj() {
        return obj;
    }

    public Expression[] getExpressions() {
        return expressions;
    }
}
