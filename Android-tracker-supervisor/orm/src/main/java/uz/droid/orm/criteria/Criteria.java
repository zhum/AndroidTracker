

package uz.droid.orm.criteria;


import uz.droid.orm.annotation.Column;
import uz.droid.orm.exception.CriteriaException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 24.10.13 11:29.
 */
public class Criteria {

    private Class clazz;
    private List<Expression> selectExpression = new ArrayList<Expression>();
    private List<DBOrder> orders = new ArrayList<DBOrder>();
    private int maxResult;


    public Criteria(Class clazz) {
        this.clazz = clazz;
    }

    public void add(Expression ex) {
        selectExpression.add(ex);
    }

    public void addOrder(DBOrder o) {
        orders.add(o);
    }


    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public int getMaxResult() {
        return maxResult;
    }

    public String getOrders() throws CriteriaException {
        if (orders.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (DBOrder order : orders) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            String columnName = getColumnName(order.getFieldName());
            sb.append(columnName).append(" ").append(order.getType() == DBOrder.ASC ? "ASC" : "DESC");
        }

        return sb.toString();
    }

    public String getSelection() throws CriteriaException {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : selectExpression) {
            if (sb.length() > 0) {
                sb.append(" AND ");
            }

            sb.append(getExpressionQuery(expression));
        }

        return sb.toString();
    }

    public String[] getValues() {
        List<String> vals = new ArrayList<String>();
        for (Expression expression : selectExpression) {
            vals.addAll(getExpressionValues(expression));
        }

        String[] array = new String[vals.size()];
        vals.toArray(array);
        return array;
    }


    public String getExpressionQuery(Expression exp) {

        if (clazz == null) {
            throw new CriteriaException("Persistent class not init");
        }

        if (exp == null) {
            throw new NullPointerException("Expression must not be null");
        }

        StringBuilder sb = new StringBuilder();
        String columnName = null;
        if(exp.getFieldName() != null) {
            columnName = getColumnName(exp.getFieldName());
        }

        switch (exp.getType()) {
            case Expression.EQUALS: {
                if(exp.getObj() == null) {
                    sb.append(columnName).append(" IS NULL");
                } else {
                    sb.append(columnName).append("=").append("?");
                }
                break;
            }

            case Expression.GREATE: {
                sb.append(columnName).append(">").append("?");
                break;
            }

            case Expression.GREATE_OR_EQUALS: {
                sb.append(columnName).append(">=").append("?");
                break;
            }

            case Expression.LESS: {
                sb.append(columnName).append("<").append("?");
                break;
            }

            case Expression.LESS_OR_EQUALS: {
                sb.append(columnName).append("<=").append("?");
                break;
            }

            case Expression.OR: {
                sb.append("(");
                for (Expression subExp : exp.getExpressions()) {
                    if(sb.length() > 1) {
                        sb.append(" OR ");
                    }
                    sb.append(getExpressionQuery(subExp));
                }
                sb.append(")");
                break;
            }

            case Expression.NOT_EQUALS: {
                if (exp.getObj() == null) {
                    sb.append(columnName).append(" IS NOT NULL");
                } else {
                    sb.append(columnName).append("<>").append("?");
                }
                break;
            }

            case Expression.NOT_NULL: {
                sb.append(columnName).append(" IS NOT NULL");
                break;
            }

            case Expression.IN: {
                sb.append(columnName).append(" IN (").append(getQArray(exp)).append(")");
                break;
            }

            case Expression.NOT_IN: {
                sb.append(columnName).append(" NOT IN (").append(getQArray(exp)).append(")");
                break;
            }
        }

        return sb.toString();
    }

    private String getQArray(Expression exp) {
        List<Serializable> values = (List<Serializable>) exp.getObj();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }

            sb.append("?");
        }

        return sb.toString();
    }

    private List<String> getExpressionValues(Expression exp) {
        List<String> vals = new ArrayList<String>(2);

        switch (exp.getType()) {
            case Expression.NOT_EQUALS:
            case Expression.EQUALS: {
                if (exp.getObj() != null) {
                    if (exp.getObj() instanceof Boolean) {
                        vals.add(((Boolean) exp.getObj()) ? "1" : "0");
                    } else {
                        vals.add(exp.getObj().toString());
                    }
                }

                break;
            }

            case Expression.OR: {
                if (exp.getExpressions() != null) {
                    for (Expression ex : exp.getExpressions()) {
                        vals.addAll(getExpressionValues(ex));
                    }
                }
                break;
            }

            case Expression.NOT_NULL: {
                break;
            }

            case Expression.IN:
            case Expression.NOT_IN: {
                List<Serializable> ids = (List<Serializable>) exp.getObj();
                for (Serializable id : ids) {
                    vals.add(id.toString());
                }
                break;
            }


            default: {
                if (exp.getObj() != null) {
                    if (exp.getObj() instanceof Boolean) {
                        vals.add(((Boolean) exp.getObj()) ? "1" : "0");
                    } else {
                        vals.add(exp.getObj().toString());
                    }
                } else {
                    vals.add(null);
                }
            }
        }

        return vals;
    }


    private String getColumnName(String fieldName) {
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new CriteriaException("No fields found by name \"" + fieldName + "\" in " +
                    "class " + clazz.getName());
        }

        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            throw new CriteriaException("Field \"" + fieldName + "\" in "
                    + clazz.getName() + " is not linked to database table column");
        }

        return column.name();
    }


}
