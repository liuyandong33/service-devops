package build.dream.devops.constants;

public class Constants extends build.dream.common.constants.Constants {
    public static final String CHANNEL_TYPE_EXEC = "exec";
    public static final String CHANNEL_TYPE_SFTP = "sftp";
    public static final String OPERATE_TYPE_START = "start";
    public static final String OPERATE_TYPE_SHUTDOWN = "shutdown";
    public static final String OPERATE_TYPE_DESTROY = "destroy";
    public static final String OPERATE_TYPE_REBOOT = "reboot";
    public static final String OPERATE_TYPE_SUSPEND = "suspend";
    public static final String OPERATE_TYPE_RESUME = "resume";
    public static final String OPERATE_TYPE_UNDEFINE = "undefine";

    public static final String ZOOKEEPER_CONNECTION_STRING = "zookeeper.connection.string";

    public static final Integer SERVICE_NODE_STATUS_RUNNING = 1;
    public static final Integer SERVICE_NODE_STATUS_STOPPED = 2;
    public static final Integer SERVICE_NODE_STATUS_WRONG = 3;

    public static final Integer HOST_STATUS_RUNNING = 1;
    public static final Integer HOST_STATUS_STOPPED = 2;

    public static final Integer HOST_TYPE_PHYSICAL_MACHINE = 1;
    public static final Integer HOST_TYPE_VIRTUAL_MACHINE = 2;
}