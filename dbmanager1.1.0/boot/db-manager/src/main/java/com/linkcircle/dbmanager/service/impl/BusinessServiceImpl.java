package com.linkcircle.dbmanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.dbmanager.entity.Business;
import com.linkcircle.dbmanager.mapper.BusinessMapper;
import com.linkcircle.dbmanager.service.BusinessService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: yangyonglian
 * @create: 2021-09-14 17:40
 **/
@Service
public class BusinessServiceImpl extends ServiceImpl<BusinessMapper, Business> implements BusinessService {
}
