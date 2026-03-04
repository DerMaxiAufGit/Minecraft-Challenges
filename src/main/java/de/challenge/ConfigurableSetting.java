package de.challenge;

public class ConfigurableSetting {

    public enum SettingType { INT, DOUBLE, BOOLEAN, STRING, LONG }

    private final String configPath;
    private final String displayName;
    private final SettingType type;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final Object step;

    public ConfigurableSetting(String configPath, String displayName, SettingType type,
                                Object defaultValue, Object minValue, Object maxValue, Object step) {
        this.configPath = configPath;
        this.displayName = displayName;
        this.type = type;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    public static ConfigurableSetting ofInt(String configPath, String displayName,
                                             int defaultValue, int min, int max, int step) {
        return new ConfigurableSetting(configPath, displayName, SettingType.INT,
                defaultValue, min, max, step);
    }

    public static ConfigurableSetting ofLong(String configPath, String displayName,
                                              long defaultValue, long min, long max, long step) {
        return new ConfigurableSetting(configPath, displayName, SettingType.LONG,
                defaultValue, min, max, step);
    }

    public static ConfigurableSetting ofDouble(String configPath, String displayName,
                                                double defaultValue, double min, double max, double step) {
        return new ConfigurableSetting(configPath, displayName, SettingType.DOUBLE,
                defaultValue, min, max, step);
    }

    public static ConfigurableSetting ofBoolean(String configPath, String displayName, boolean defaultValue) {
        return new ConfigurableSetting(configPath, displayName, SettingType.BOOLEAN,
                defaultValue, null, null, null);
    }

    public static ConfigurableSetting ofString(String configPath, String displayName, String defaultValue) {
        return new ConfigurableSetting(configPath, displayName, SettingType.STRING,
                defaultValue, null, null, null);
    }

    public String getConfigPath() { return configPath; }
    public String getDisplayName() { return displayName; }
    public SettingType getType() { return type; }
    public Object getDefaultValue() { return defaultValue; }
    public Object getMinValue() { return minValue; }
    public Object getMaxValue() { return maxValue; }
    public Object getStep() { return step; }
}
