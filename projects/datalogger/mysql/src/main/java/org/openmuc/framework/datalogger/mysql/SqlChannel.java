package org.openmuc.framework.datalogger.mysql;

import org.openmuc.framework.options.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlChannel extends SqlConfigs {
    private static final Logger logger = LoggerFactory.getLogger(SqlChannel.class);

    public static int TYPE_LENGTH_DEFAULT = 10;
    public static String TYPE_DEFAULT = "FLOAT";
    public static String TYPE_NOT_NULL = " NOT NULL";
    public static String[] TYPES = new String[] {
        "FLOAT",
        "REAL",
        "BIGINT",
        "INT",
        "SMALLINT",
        "TINYINT",
        "BIT",
        "VARBINARY",
        "VARCHAR"
    };

    private static String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS %s ("
            + "time INT UNSIGNED NOT NULL, "
            + "data %s, "
            + "PRIMARY KEY (time)"
            + ") ENGINE=MYISAM";

    public static String QUERY_SELECT_SINGLEROW = "SELECT %s FROM halm.%s WHERE timestamp >= \"%s\" AND timestamp <= \"%s\"";
    public static String QUERY_SELECT_DATETIME = "SELECT %s FROM %s.%s WHERE testtime >= \"%s\" AND testtime <= \"%s and testdate >= \"%s\" AND testdate >= \"%s\" ";
    public static String QUERY_SELECT_MULTIPLEROW = "SELECT %s FROM %s WHERE SVNAME = '%s' AND  timestamp >= '%s' AND timestamp <= '%s'";
    private static String QUERY_INSERT = "INSERT INTO %s (time,data) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE data=VALUES(data)";

    @Setting(mandatory= false)
    protected String index = "time";

    @Setting(mandatory= false)
    protected String column;

    @Setting(mandatory= false)
    protected String data;

    @Setting(mandatory= false)
    protected String key = null;

    public String getColumn() {
        return column;
    }

//    public static SqlChannel create(SqlCallbacks callbacks, Transaction transaction, Integer id, 
//            String table, String type, boolean empty) throws EmoncmsException {
//        
//        SqlChannel feed = connect(callbacks, redis, id, table);
//        if (type == null) {
//            type = TYPE_DEFAULT;
//        }
//        else if (!Arrays.asList(TYPES).contains(type) && 
//                !type.startsWith("VARCHAR(") && !type.startsWith("VARBINARY(")) {
//            throw new EmoncmsException("Value type not allowed: "+type);
//        }
//        if (!empty) {
//            type += TYPE_NOT_NULL;
//        }
//        String query = String.format(QUERY_CREATE, feed.table, type);
//        logger.debug("Query  {}", query);
//        
//        transaction.execute(query);
//        return feed;
//    }
//
//    @Override
//    public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
//        String query = String.format(QUERY_SELECT, table, start, end);
//        logger.debug("Query {}", query);
//        
//        LinkedList<Timevalue> timevalues = new LinkedList<Timevalue>();
//        try (Connection connection = callbacks.getConnection()) {
//            try (Statement statement = connection.createStatement()) {
//                try (ResultSet result = statement.executeQuery(query)) {
//                    while (result.next()) {
//                        long time = result.getLong(COLUMN_TIME)*1000;
//                        double value = result.getInt(COLUMN_DATA);
//                        
//                        timevalues.add(new Timevalue(time, value));
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            throw new SqlException(e);
//        }
//        return timevalues;
//    }
//
//    @Override
//    public void insertData(Timevalue timevalue) throws EmoncmsException {
//        try (Transaction transaction = callbacks.getTransaction()) {
//            insertData(transaction, timevalue.getTime(), timevalue.getValue());
//            
//        } catch (Exception e) {
//            throw new SqlException(e);
//        }
//        try {
//            cacheData(timevalue.getTime(), timevalue.getValue());
//        }
//        catch (RedisUnavailableException ignore) {}
//    }
//
//    public void insertData(Transaction transaction, long timestamp, double data) throws EmoncmsException {
//        int time = (int) Math.round((double) timestamp/1000.0);
////      if (id != null) {
////          transaction.execute(String.format(QUERY_UPDATE, time, data, id));
////      }
//        String query = String.format(QUERY_INSERT, table, time, data);
//        logger.debug("Query {}", query);
//        
//        transaction.execute(query);
//    }

}
