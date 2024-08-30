package com.dtstack.taier.datasource.plugin.common.session;

public final class SessionConstants {
    public static final String SET_PREFIX = "set:";
    public static final String ENV_PREFIX = "env:";
    public static final String SYSTEM_PREFIX = "system:";
    public static final String HIVECONF_PREFIX = "hiveconf:";
    public static final String HIVEVAR_PREFIX = "hivevar:";
    public static final String METACONF_PREFIX = "metaconf:";
    public static final String USE_CATALOG = "use:catalog";
    public static final String USE_DATABASE = "use:database";

    public static final String SPARK_PREFIX = "spark.";

    // 私有构造函数防止实例化
    private SessionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}