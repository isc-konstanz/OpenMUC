package org.openmuc.framework.lib.sql;

public interface SqlSettings {

    public String getDatabaseUrl();

    public String getDatabaseName();

    public String getDatabaseDriver();

    public String getDatabaseType();

    public String getDatabaseUser();

    public String getDatabasePassword();

    public String getTable();

    public TableType getTableType();

    public int getTimeResolution();

    public String getTimeFormat();

    public String getIndexColumn();

    public IndexType getIndexType();

}
