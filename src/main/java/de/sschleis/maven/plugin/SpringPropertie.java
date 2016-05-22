package de.sschleis.maven.plugin;

public class SpringPropertie {

    private String name;
    private String type;
    private String defaultValue;
    private String fileName;

    public SpringPropertie(String name, String type, String defaultValue, String fileName) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.fileName = fileName;
    }

    public SpringPropertie(String line) {

        final int start = line.indexOf("${");
        final int end = line.indexOf("}");
        if (line.contains(":")) {
            final int dp = line.indexOf(":");
            name = line.substring(start + 2, dp);
            defaultValue = line.substring(dp + 1, end);
        } else {
            name = line.substring(start + 2, end);
        }


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "SpringPropertie{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public String toCsv() {
        return name + ", " + defaultValue + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpringPropertie that = (SpringPropertie) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
