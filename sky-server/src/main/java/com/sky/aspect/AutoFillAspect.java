package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自定义切面，用于处理公共字段自动填充处理逻辑
 */
@Aspect // 说明这是一个切面类
@Component // 说明这是一个组件，需要被Spring容器管理
@Slf4j
public class AutoFillAspect
{
    /**
     * 切入点，说明需要对哪些方法进行拦截
     * 通过切面表达式说明需要对哪个方法进行拦截，此处为mapper包下面的所有方法，且方法上面有AutoFill注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut()
    {
    }

    /**
     * 通知，表示对方法进行增强，此处为在方法执行之前进行增强，因为如果SQL语句执行完了再进行公共字段填充就没有意义了
     * 方法传入的参数是连接点，表示哪个方法被拦截了，以及可以通过参数知道被拦截的参数的值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint)
    {
        log.info("开始进行公共字段的填充...");

        // 首先应该获取当前被拦截的方法上面的数据库的操作类型，因为不同的操作类型需要处理的自动填充处理逻辑是不同的
        // 获得方法签名对象，方法签名对象是指方法的描述对象，可以通过方法签名对象获取到方法的所有信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取到方法上面的AutoFill注解对象
        OperationType operationType = autoFill.value(); // 获取到数据库的操作类型

        // 获取到被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0)
        {
            return;
        }
        /**
         * 获取到实体对象，因为实体对象是第一个参数
         * 所以直接获取第一个参数即可，那么后续如果有其他的insert方法
         * 也要保证实体对象放在第一个位置，这是本项目的规定
         */
        Object entity = args[0];

        // 准备赋值的具体数据
        LocalDateTime now = LocalDateTime.now(); // 获取到当前时间
        Long currentId = BaseContext.getCurrentId();

        // 根据当前操作的不同类型，为对应的属性值通过反射进行赋值
        if (operationType == OperationType.INSERT)
        {
            // 为四个公共字段赋值
            // 由于对应的实体类加了@Data注解，所以需要先获得set方法然后才能进行赋值的操作
            try
            {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (operationType == OperationType.UPDATE)
        {
            // 为两个公共字段赋值
            try
            {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
