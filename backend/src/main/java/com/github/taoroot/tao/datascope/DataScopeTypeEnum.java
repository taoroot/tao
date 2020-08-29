package com.github.taoroot.tao.datascope;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataScopeTypeEnum {

    ALL(1, "全部数据权限"),
    THIS_LEVEL(2, "本部门数据权限"),
    THIS_LEVEL_CHILDREN(3, "本部门及以下数据权限"),
    CUSTOMIZE(4, "自定数据权限"),
    OWN(5, "仅本人数据权限");

    @EnumValue
    private final int value;
    private final String description;

    public static DataScopeTypeEnum valueOf(int type) {
        for (DataScopeTypeEnum typeVar : DataScopeTypeEnum.values()) {
            if (typeVar.getValue() == type) {
                return typeVar;
            }
        }
        return ALL;
    }
}
