package uz.droid.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import uz.droid.orm.annotation.Column;
import uz.droid.orm.annotation.Id;
import uz.droid.orm.annotation.Table;
import uz.droid.orm.criteria.Criteria;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 19.09.13 12:58.
 */

public abstract class GenericDao<T, PK extends Serializable> {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_REAL = "REAL";

    private Context context;
    private Class<T> persistentClass;

    private String TABLE;

    protected GenericDao(Context context, Class<T> persistentClass) {
        this.context = context;
        this.persistentClass = persistentClass;
        this.TABLE = persistentClass.getAnnotation(Table.class).name();
    }

    public List<T> getAll() {
        List<T> result = new ArrayList<T>();

        SQLiteDatabase db = getReadableDB();
        Cursor c = db.query(TABLE, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                T persistentObject = getPersistentObject(c);
                result.add(persistentObject);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return result;
    }

    public List<T> getAll(Criteria criteria) {

        String selection = null;
        String[] selectionArgs = null;
        String orders = null;
        String limit = null;
        if (criteria != null) {
            selection = criteria.getSelection();
            selectionArgs = criteria.getValues();
            orders = criteria.getOrders();
            limit = criteria.getMaxResult() > 0 ? String.valueOf(criteria.getMaxResult()) : null;
        }

        List<T> result = new ArrayList<T>();
        SQLiteDatabase db = getReadableDB();
        Cursor c = db.query(TABLE, null, selection, selectionArgs, null, null, orders, limit);
        if (c.moveToFirst()) {
            do {
                T persistentObject = getPersistentObject(c);
                result.add(persistentObject);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return result;
    }

    public int deleteAll(Criteria criteria) {
        String selection = null;
        String[] selectionArgs = null;
        if (criteria != null) {
            selection = criteria.getSelection();
            selectionArgs = criteria.getValues();
        }

        SQLiteDatabase db = getWritableDB();
        int count = db.delete(TABLE, selection, selectionArgs);
        db.close();
        return count;
    }

    public boolean delete(PK id) {
        String selection = getIdColumnName().concat("=?");
        String[] selectionArgs = new String[]{id.toString()};
        SQLiteDatabase db = getWritableDB();
        int count = db.delete(TABLE, selection, selectionArgs);
        db.close();
        return count > 0;
    }

    public T get(PK id) {
        SQLiteDatabase db = getReadableDB();
        Cursor c = db.query(TABLE, null, getIdColumnName().concat("=?"), new String[]{id.toString()}, null, null, null);

        T persistentObject = null;
        if (c.moveToFirst()) {
            persistentObject = getPersistentObject(c);
        }

        c.close();
        db.close();
        return persistentObject;
    }

    public T saveOrUpdate(T obj) {
        SQLiteDatabase db = getWritableDB();

        PK id = getIdValue(obj);

        int rowsAffected = 0;
        if (id != null) {
            rowsAffected = db.update(TABLE, objectToCV(obj), getIdColumnName().concat("=?"), new String[]{getIdValue(obj).toString()});
        }


        if (rowsAffected > 0) {
            db.close();
            return this.get(id);
        }

        Long newId = db.insert(TABLE, null, objectToCV(obj));
        if (newId > 0) {
            id = (PK) newId;
        }
        db.close();
        return this.get(id);
    }

    public void saveOrUpdateAll(List<T> objects) {
        if (objects == null && objects.size() == 0) {
            return;
        }

        SQLiteDatabase db = getWritableDB();
        for (T obj : objects) {
            PK id = getIdValue(obj);

            int rowsAffected = 0;
            if (id != null) {
                rowsAffected = db.update(TABLE, objectToCV(obj), getIdColumnName().concat("=?"), new String[]{getIdValue(obj).toString()});
            }

            if (rowsAffected > 0) {
                continue;
            }

            Long newId = db.insert(TABLE, null, objectToCV(obj));
            if (newId > 0) {
                //
            }

        }

        db.close();
    }

    public Criteria createCriteria() {
        return new Criteria(persistentClass);
    }


    public final String getOnCreateSQL() {

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ").append(TABLE).append(" (");
        List<Field> sqlFields = new ArrayList<Field>();

        for (Field field : persistentClass.getDeclaredFields()) {
            if (field.getAnnotation(Column.class) != null) {
                sqlFields.add(field);
            }
        }

        Collections.sort(sqlFields, fieldComparator);

        boolean isFirst = true;
        for (Field field : sqlFields) {
            Column column = field.getAnnotation(Column.class);
            String type = getColumnType(field.getType());

            if (!isFirst) {
                sql.append(", "); // separate columns
            } else {
                isFirst = false;
            }

            sql.append(column.name()).append(" ").append(type);

            Id id;
            if ((id = field.getAnnotation(Id.class)) != null) {
                sql.append(" ").append("PRIMARY KEY");
                if (type.equals(TYPE_INTEGER) && id.autoIncrement()) {
                    sql.append(" ").append("AUTOINCREMENT");
                }
            }
        }

        sql.append(") ");

        return sql.toString();
    }

    public String getDropSQL() {
        return "DROP TABLE IF EXISTS " + TABLE;
    }

    private String getColumnType(Class clazz) {
        String type = TYPE_TEXT;
        if (clazz == Long.class || clazz == long.class
                || clazz == Integer.class || clazz == int.class
                || clazz == Boolean.class || clazz == boolean.class) {
            type = TYPE_INTEGER;
        } else if (clazz == Double.class || clazz == double.class
                || clazz == Float.class || clazz == float.class) {
            type = TYPE_REAL;
        }

        return type;
    }

    private ContentValues objectToCV(T object) {
        ContentValues cv = new ContentValues();

        for (Field field : persistentClass.getDeclaredFields()) {
            try {
                putToCV(object, field, cv);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return cv;
    }

    private T getPersistentObject(Cursor c) {
        try {
            T persistentObject = persistentClass.newInstance();

            for (Field field : persistentClass.getDeclaredFields()) {
                setField(persistentObject, field, c);
            }

            return persistentObject;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getIdColumnName() {
        for (Field field : persistentClass.getDeclaredFields()) {
            if ((field.getAnnotation(Id.class)) != null) {
                Column column = field.getAnnotation(Column.class);
                return column.name();
            }
        }

        return null;
    }

    private PK getIdValue(T obj) {
        for (Field field : persistentClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                try {
                    field.setAccessible(true);
                    return (PK) field.get(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void setIdValue(PK id, T obj) {
        for (Field field : persistentClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                field.setAccessible(true);
                // TODO set id value;
            }
        }
    }

    private void putToCV(T obj, Field f, ContentValues cv) throws IllegalAccessException {
        Column column = f.getAnnotation(Column.class);
        if (column == null) {
            return;
        }

        f.setAccessible(true);
        String columnName = column.name();
        Class cls = f.getType();

        if (cls == Boolean.class) {
            Boolean b = (Boolean) f.get(obj);
            b = b != null && b;
            int bool = b ? 1 : 0;
            cv.put(columnName, bool);
            return;
        }

        if (cls == boolean.class) {
            int bool = f.getBoolean(obj) ? 1 : 0;
            cv.put(columnName, bool);
            return;
        }

        if (cls == Double.class) {
            cv.put(columnName, (Double) f.get(obj));
            return;
        }

        if (cls == double.class) {
            cv.put(columnName, f.getDouble(obj));
            return;
        }

        if (cls == Float.class) {
            cv.put(columnName, (Float) f.get(obj));
            return;
        }

        if (cls == float.class) {
            cv.put(columnName, f.getFloat(obj));
            return;
        }

        if (cls == Integer.class) {
            cv.put(columnName, (Integer) f.get(obj));
            return;
        }

        if (cls == int.class) {
            cv.put(columnName, f.getInt(obj));
            return;
        }

        if (cls == Long.class) {
            cv.put(columnName, (Long) f.get(obj));
            return;
        }

        if (cls == long.class) {
            cv.put(columnName, f.getLong(obj));
            return;
        }

        if (cls == Short.class) {
            cv.put(columnName, (Short) f.get(obj));
            return;
        }

        if (cls == short.class) {
            cv.put(columnName, f.getShort(obj));
            return;
        }

        if (cls == String.class) {
            cv.put(columnName, (String) f.get(obj));
            return;
        }

        putOtherTypeToCV(obj, f, columnName, cv);
    }

    private void setField(Object obj, Field f, Cursor c) throws IllegalAccessException {
        Column column = f.getAnnotation(Column.class);
        if (column == null) {
            return;
        }

        String columnName = column.name();
        Class cls = f.getType();

        if (c.isNull(c.getColumnIndex(columnName))) {
            return;
        }

        f.setAccessible(true);
        if (cls == Boolean.class) {
            Boolean b = c.getInt(c.getColumnIndex(columnName)) == 1;
            f.set(obj, b);
            return;
        }

        if (cls == boolean.class) {
            f.setBoolean(obj, c.getInt(c.getColumnIndex(columnName)) == 1);
            return;
        }

        if (cls == Double.class) {
            f.set(obj, c.getDouble(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == double.class) {
            f.setDouble(obj, c.getDouble(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == Float.class) {
            f.set(obj, c.getFloat(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == float.class) {
            f.setFloat(obj, c.getFloat(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == Integer.class) {
            f.set(obj, c.getInt(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == int.class) {
            f.setInt(obj, c.getInt(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == Long.class) {
            f.set(obj, c.getLong(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == long.class) {
            f.setLong(obj, c.getLong(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == Short.class) {
            f.set(obj, c.getShort(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == short.class) {
            f.setShort(obj, c.getShort(c.getColumnIndex(columnName)));
            return;
        }

        if (cls == String.class) {
            f.set(obj, c.getString(c.getColumnIndex(columnName)));
            return;
        }

        setOtherTypeField(obj, f, columnName, c);
    }

    protected abstract void putOtherTypeToCV(Object obj, Field f, String columnName, ContentValues cv);

    protected abstract void setOtherTypeField(Object obj, Field f, String columnName, Cursor cursor);

    protected abstract SQLiteDatabase getReadableDB();

    protected abstract SQLiteDatabase getWritableDB();


    /*
     *Sorting fields by columnOrder value of @Column annotation;
      */
    private static Comparator<Field> fieldComparator = new Comparator<Field>() {
        @Override
        public int compare(Field f1, Field f2) {
            Column c1 = f1.getAnnotation(Column.class);
            Column c2 = f2.getAnnotation(Column.class);

            return c1.columnOrder() - c2.columnOrder();
        }
    };


    public Context getContext() {
        return context;
    }
}
