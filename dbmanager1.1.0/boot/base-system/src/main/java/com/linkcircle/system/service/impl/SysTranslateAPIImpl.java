package com.linkcircle.system.service.impl;

import com.linkcircle.boot.common.api.desform.ISysTranslateAPI;
import com.linkcircle.boot.common.system.vo.DictModel;
import com.linkcircle.system.service.ISysCategoryService;
import com.linkcircle.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 表单设计器翻译API接口（system实现类）
 *
 * @author sunjianlei
 */
@Component
public class SysTranslateAPIImpl implements ISysTranslateAPI {

    @Autowired
    ISysCategoryService sysCategoryService;
    @Autowired
    ISysDictService sysDictService;

    @Override
    public List<String> categoryLoadDictItem(String ids) {
        return sysCategoryService.loadDictItem(ids, false);
    }

    @Override
    public List<String> dictLoadDictItem(String dictCode, String keys) {
        String[] params = dictCode.split(",");
        return sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys, false);
    }

    @Override
    public List<DictModel> dictGetDictItems(String dictCode) {
        List<DictModel> ls = sysDictService.getDictItems(dictCode);
        if (ls == null) {
            ls = new ArrayList<>();
        }
        return ls;
    }

    @Override
    public List<DictModel> dictLoadDict(String dictCode, String keyword, Integer pageSize) {
        return sysDictService.loadDict(dictCode, keyword, pageSize);
    }

}
