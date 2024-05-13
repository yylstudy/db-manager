package com.linkcircle.boot.common.aspect;

import com.linkcircle.boot.common.aspect.annotation.JRepeat;
import com.linkcircle.boot.common.client.RedissonLockClient;
import com.linkcircle.boot.common.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/12/15 16:59
 */
@Aspect
@Component
public class RepeatSubmitAspect {
    @Resource
    private RedissonLockClient redissonLockClient;

    /***
     * 定义controller切入点拦截规则，拦截JRepeat注解的业务方法
     */
    @Pointcut("@annotation(jRepeat)")
    public void pointCut(JRepeat jRepeat) {
    }

    /**
     * AOP分布式锁拦截
     *
     * @param joinPoint
     * @return
     * @throws Exception
     */
    @Around("pointCut(jRepeat)")
    public Object repeatSubmit(ProceedingJoinPoint joinPoint, JRepeat jRepeat) throws Throwable {
        if (Objects.nonNull(jRepeat)) {
            // 获取参数
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String key = parseContent(signature.getMethod(),args,jRepeat.lockKey());
            boolean isLocked = false;
            try {
                isLocked = redissonLockClient.fairLock(key, TimeUnit.SECONDS, jRepeat.lockTime());
                // 如果成功获取到锁就继续执行
                if (isLocked) {
                    // 执行进程
                    return joinPoint.proceed();
                } else {
                    // 未获取到锁
                    throw new BusinessException("请勿重复提交");
                }
            } finally {
                // 如果锁还存在，在方法执行完成后，释放锁
                if (isLocked) {
                    redissonLockClient.unlock(key);
                }
            }
        }

        return joinPoint.proceed();
    }
    private Map<Method, Map<String,RepeatSubmitContent>> map = new ConcurrentHashMap<>();

    private String parseContent(Method method, Object[] args,String content){
        GenericTokenParser genericTokenParser = new GenericTokenParser("${", "}", fillContent->{
            Map<String,RepeatSubmitContent> parserMap = map.computeIfAbsent(method,key->new ConcurrentHashMap<>());
            RepeatSubmitContent waterLogContent = parserMap.computeIfAbsent(fillContent,key2->resolveValue(method,key2));
            int paramIndex = waterLogContent.getParamIndex();
            Object param = args[paramIndex];
            if(waterLogContent.isPrimaryType()){
                return String.valueOf(param);
            }else{
                MetaObject metaObject = SystemMetaObject.forObject(param);
                Object obj = metaObject.getValue(waterLogContent.getNewFillContent());
                return String.valueOf(obj);
            }
        });
        //解析${}中的值
        content = genericTokenParser.parse(content);
        return content;
    }

    private RepeatSubmitContent resolveValue(Method method,String fillContent){
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        int index = fillContent.indexOf(".");
        RepeatSubmitContent content = new RepeatSubmitContent();
        if(index<0){
            content.setPrimaryType(true);
            for(int i=0;i<paramNames.length;i++){
                if(paramNames[i].equals(fillContent)){
                    content.setParamIndex(i);
                    break;
                }
            }
        }else{
            content.setPrimaryType(false);
            String paramName = fillContent.substring(0,index);
            String field = fillContent.substring(index+1);
            content.setNewFillContent(field);
            for(int i=0;i<paramNames.length;i++){
                if(paramNames[i].equals(paramName)){
                    content.setParamIndex(i);
                    break;
                }
            }
        }
        return content;
    }

    @Getter
    @Setter
    static class RepeatSubmitContent{
        /**
         * 参数下标
         */
        private int paramIndex;
        /**
         * 是否是基本类型和字符串，这里表现为是否包含.
         */
        private boolean isPrimaryType;

        private String newFillContent;


    }
}
