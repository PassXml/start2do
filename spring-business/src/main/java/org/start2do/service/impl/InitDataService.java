package org.start2do.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.DefaultDataItem;
import org.start2do.constant.Constant;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.enums.YesOrNoType;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.SysDict.Type;
import org.start2do.entity.business.SysDictItem;
import org.start2do.entity.business.query.QSysDict;
import org.start2do.service.servlet.SysDictService;
import org.start2do.util.spring.SpringInitListenerUtil.WaitInitCompleteRunner;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "start2do.ebean",
    name = {"enable", "enable-setting-service"},
    havingValue = "true",
    matchIfMissing = true)
public class InitDataService implements WaitInitCompleteRunner {

  private final SysDictService sysDictService;

  private final BusinessConfig config;

  public static void initDict(DefaultDataItem item) {
    if (new QSysDict().dictKey.eq(item.getKey()).exists()) {
      return;
    }
    SysDict dict = new SysDict(item.getKey(), item.getDesc(), Type.SYSTEM);
    dict.setCreatePerson("SYSTEM");
    dict.setCreateTime(LocalDateTime.now());
    dict.save();
    for (String s : item.getValue()) {
      String[] split = s.split(":");
      if (split.length == 2) {
        SysDictItem dictItem = new SysDictItem(dict.getId(), split[1], split[0], 0);
        dictItem.save();
      }
    }
  }

  @Override
  public void init() {
    if (config.getDefaultData() == null) {
      return;
    }
    initDict(new DefaultDataItem(true, Constant.TYPE_USER_AUTH, List.of(), "用户认证类型"));
    initDict(new DefaultDataItem(false, Constant.KEY_FILE_DOWNLOAD_HOST, List.of(), "文件下载时host前缀"));
    initDict(
        new DefaultDataItem(false, Constant.KEY_FILE_DOWNLOAD_USE_PATH, List.of(), "是否启用路径下载"));
    for (DefaultDataItem item : config.getDefaultData()) {
      if (item.isDict()) {
        initDict(item);
      } else {
        initSetting(item);
      }
    }
  }

  public static void initSetting(DefaultDataItem item) {
    if (new QSysSetting().type.eq(Constant.TYPE_SYSTEM_SETTING).key.eq(item.getKey()).exists()) {
      return;
    }
    SysSetting set =
        new SysSetting(Constant.TYPE_SYSTEM_SETTING, item.getKey(), EnableType.Enable)
            .setIsBuiltIn(YesOrNoType.Yes)
            .setValue(
                Optional.ofNullable(item.getValue())
                    .map(strings -> strings.stream().findFirst())
                    .map(Optional::get)
                    .orElse(null));
    set.setCreatePerson("SYSTEM");
    set.setCreateTime(LocalDateTime.now());
    set.save();
  }
}
