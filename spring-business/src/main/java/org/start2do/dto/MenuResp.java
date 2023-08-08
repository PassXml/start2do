package org.start2do.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.entity.business.SysDictItem;
import org.start2do.entity.business.query.QSysDictItem;
import org.start2do.service.SysDictItemService;
import org.start2do.util.spring.SpringBeanUtil;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuResp {

    private String label;
    private Object value;

    public MenuResp(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public static List<MenuResp> toList(IDictItem[] value) {
        return Arrays.stream(value).map(t -> new MenuResp(t.getLabel(), t.getValue())).toList();
    }

    public static List<MenuResp> toList(IDictItem[] value, Class tClass) {
        return reWriteDictionary(toList(value), tClass);
    }

    /**
     * 重写字典值内容
     *
     * @return
     */
    public static List<MenuResp> reWriteDictionary(List<MenuResp> list, Class tClass) {
        Optional.ofNullable(SpringBeanUtil.getBean(SysDictItemService.class)).ifPresent(t -> {
            List<SysDictItem> items = t.findAll(new QSysDictItem().sysDict.dictName.eq(tClass.getName()));
            for (MenuResp resp : list) {
                for (SysDictItem item : items) {
                    if (item.getItemData().equals(resp.getValue())) {
                        resp.setLabel(item.getItemName());
                    }
                }
            }
        });
        return list;
    }

    public static List<MenuResp> toList(List<? extends IDictItem> systems) {
        if (systems == null) {
            return null;
        }
        return systems.stream().map(t -> new MenuResp(t.getLabel(), t.getValue())).toList();
    }
}
