package uz.droid.orm;

import uz.droid.orm.criteria.Criteria;
import uz.droid.orm.model.DBObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 20.01.14 0:38.
 */
public class DatabaseQueue extends Thread {

    private static DatabaseQueue instance;

    public static void initInstance() {
        instance = new DatabaseQueue();
    }

    public static DatabaseQueue getInstance() {
        return instance;
    }

    private DatabaseQueue() {
    }

    private static final int SAVE = 0;
    private static final int GET = 1;
    private static final int GET_ALL = 2;
    private static final int GET_ALL_CRITERIA = 3;
    private static final int SAVE_ALL = 4;
    private static final int DELETE = 5;
    private static final int DELETE_ALL_CRITERIA = 6;

    private static boolean isActionRunning = false;


    private List<DBAction> actionList = new ArrayList<DBAction>(10);

    public void save(GenericDao dao, DBObject obj, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(SAVE, dao, obj, dbCallback), true);
    }

    public void saveAll(GenericDao dao, List<? extends DBObject> objs, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(SAVE_ALL, dao, objs, dbCallback), true);
    }

    public void get(GenericDao dao, Serializable id, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(GET, dao, id, dbCallback), true);
    }

    public void getAll(GenericDao dao, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(GET_ALL, dao, dbCallback), true);
    }

    public void getAll(GenericDao dao, Criteria criteria, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(GET_ALL_CRITERIA, dao, criteria, dbCallback), true);
    }

    public void delete(GenericDao dao, Serializable id, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(DELETE, dao, id, dbCallback), true);
    }

    public void deleteAll(GenericDao dao, Criteria criteria, DBCallback dbCallback) {
        addOrRemoveAction(new DBAction(DELETE_ALL_CRITERIA, dao, criteria, dbCallback), true);
    }

    private synchronized void addOrRemoveAction(DBAction action, boolean isAdding) {
        if (isAdding) {
            actionList.add(action);
            if (!isActionRunning) {
                isActionRunning = false;
                if(getState() == State.NEW) {
                    start();
                } else {
                    run();
                }
            }
        } else {
            actionList.remove(action);
        }
    }

    @Override
    public void run() {
        isActionRunning = true;

        while (true) {

            synchronized (actionList) {
                if(actionList.size() == 0) {
                    break;
                }
            }

            DBAction action = actionList.get(0);
            switch (action.type) {
                case SAVE: {
                    saveToDB(action);
                    break;
                }
                case GET: {
                    getFromDB(action);
                    break;
                }
                case GET_ALL: {
                    getAllFromDB(action);
                    break;
                }

                case GET_ALL_CRITERIA: {
                    getAllWithCriteriaFromDB(action);
                    break;
                }

                case SAVE_ALL: {
                    saveAllToDB(action);
                    break;
                }

                case DELETE: {
                    deleteFromDB(action);
                    break;
                }

                case DELETE_ALL_CRITERIA: {
                    deleteAllWithCriteriaFromDB(action);
                    break;
                }
            }

        }

        isActionRunning = false;
    }

    private void saveToDB(DBAction action) {
        DBObject object = (DBObject)action.object;
        GenericDao dao = action.dao;
        Object obj = dao.saveOrUpdate(object);
        addOrRemoveAction(action, false);
        if(action.dbCallback != null) {
            action.dbCallback.onComplete(obj);
        }
    }

    private void saveAllToDB(DBAction action) {
        List<DBObject> objects = (List<DBObject>) action.object;
        GenericDao dao = action.dao;
        dao.saveOrUpdateAll(objects);
        addOrRemoveAction(action, false);
        if(action.dbCallback != null) {
            action.dbCallback.onComplete(null);
        }
    }

    private void getFromDB(DBAction action) {
        Serializable id = (Serializable)action.object;
        GenericDao dao = action.dao;
        DBObject result = (DBObject)dao.get(id);
        addOrRemoveAction(action, false);
        if(action.dbCallback != null) {
            action.dbCallback.onComplete(result);
        }
    }

    private void getAllFromDB(DBAction action) {
        GenericDao dao = action.dao;
        List<DBObject> result = (List<DBObject>)dao.getAll();
        addOrRemoveAction(action, false);
        if(action.dbCallback != null) {
            action.dbCallback.onComplete(result);
        }
    }

    private void getAllWithCriteriaFromDB(DBAction action) {
        Criteria criteria = (Criteria)action.object;
        GenericDao dao = action.dao;
        List<DBObject> result = (List<DBObject>)dao.getAll(criteria);
        addOrRemoveAction(action, false);
        if(action.dbCallback != null) {
            action.dbCallback.onComplete(result);
        }
    }

    private void deleteFromDB(DBAction action) {
        GenericDao dao = action.dao;
        Serializable id = (Serializable)action.object;
        boolean isDeleted = dao.delete(id);
        addOrRemoveAction(action, false);
        if (action.dbCallback != null) {
            action.dbCallback.onComplete(isDeleted);
        }
    }

    private void deleteAllWithCriteriaFromDB(DBAction action) {
        Criteria criteria = (Criteria) action.object;
        GenericDao dao = action.dao;
        int deletedCount = dao.deleteAll(criteria);
        addOrRemoveAction(action, false);
        if (action.dbCallback != null) {
            action.dbCallback.onComplete(deletedCount);
        }
    }


    private class DBAction {
        int type;
        GenericDao dao;
        Object object;
        DBCallback dbCallback;

        private DBAction(int type, GenericDao dao, Object object, DBCallback dbCallback) {
            this.type = type;
            this.dao = dao;
            this.object = object;
            this.dbCallback = dbCallback;
        }

        private DBAction(int type, GenericDao dao, DBCallback dbCallback) {
            this.type = type;
            this.dao = dao;
            this.dbCallback = dbCallback;
        }
    }
}
