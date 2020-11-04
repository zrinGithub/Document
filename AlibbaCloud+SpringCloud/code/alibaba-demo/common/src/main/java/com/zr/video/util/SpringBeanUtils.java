package com.zr.video.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 *
 * @author zhangr
 * 2020/9/10 16:48
 */
@Slf4j
public class SpringBeanUtils {
    public static <T, D> List<T> copy(List<D> sourceObjs, Class<T> clazz) {
        if (sourceObjs == null) {
            return Collections.emptyList();
        } else {
            int len = sourceObjs.size();
            List list = new ArrayList(len);
            Object t = null;

            for(int i = 0; i < len; ++i) {
                Object d = sourceObjs.get(i);
                t = copyProperties(d, clazz);
                list.add(t);
            }

            return list;
        }
    }

    public static <T, D> T copyProperties(D sourceObj, Class<T> clazz) {
        if (sourceObj == null) {
            return null;
        } else {
            T t = null;

            try {
                t = clazz.newInstance();
                org.springframework.beans.BeanUtils.copyProperties(sourceObj, t);
            } catch (IllegalAccessException var4) {
                log.error("自动转换失败", var4);
            } catch (InstantiationException var5) {
                log.error("自动转换失败", var5);
            }

            return t;
        }
    }
}
