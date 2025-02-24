package com.ruoyi.generator.domain;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 生成数据信息
 */
public class GenInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 生成数量
     */
    private Long genNumbers;

    /**
     * 字段信息
     */
    private List<Column> tableColumnValues;

    public static class Column {
        private String columnName;  //字段名
        private String columnComment;   //字段描述
        private String columnType;  //字段类型
        private boolean pk;  //主键
        private String javaType;    //Java类型
        private String javaField;   //Java字段
        private boolean required;   //是否必须
        private int isSole;     //是否唯一
        private String value;   //值

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnComment() {
            return columnComment;
        }

        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public boolean isPk() {
            return pk;
        }

        public void setPk(boolean pk) {
            this.pk = pk;
        }

        public String getJavaType() {
            return javaType;
        }

        public void setJavaType(String javaType) {
            this.javaType = javaType;
        }

        public String getJavaField() {
            return javaField;
        }

        public void setJavaField(String javaField) {
            this.javaField = javaField;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public int getIsSole() {
            return isSole;
        }

        public void setIsSole(int isSole) {
            this.isSole = isSole;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "\nColumn{" +
                    "columnName='" + columnName + '\'' +
                    ", columnComment='" + columnComment + '\'' +
                    ", columnType='" + columnType + '\'' +
                    ", pk=" + pk +
                    ", javaType='" + javaType + '\'' +
                    ", javaField='" + javaField + '\'' +
                    ", required=" + required +
                    ", isSole=" + isSole +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @NotBlank(message = "表名不能为空！！！")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getGenNumbers() {
        return genNumbers;
    }

    public void setGenNumbers(Long genNumbers) {
        this.genNumbers = genNumbers;
    }

    public List<Column> getTableColumnValues() {
        return tableColumnValues;
    }

    public void setTableColumnValues(List<Column> tableColumnValues) {
        this.tableColumnValues = tableColumnValues;
    }

    @Override
    public String toString() {
        return "GenInfo{" +
                "tableName='" + tableName + '\'' +
                ", genNumbers=" + genNumbers +
                ", tableColumnValues=" + tableColumnValues +
                '}';
    }
}
