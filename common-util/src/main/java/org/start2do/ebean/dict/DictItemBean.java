package org.start2do.ebean.dict;

public class DictItemBean {

    public static DictItemBean of(String value, String label) {
        final DictItemBean dictItemBean = new DictItemBean();
        dictItemBean.setValue(value);
        dictItemBean.setLabel(label);
        return dictItemBean;
    }

    private String value;

    private String label;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
