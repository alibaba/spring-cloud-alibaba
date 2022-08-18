/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appactive.demo.common.filter;

import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.support.log.LogUtil;
import io.appactive.support.sys.JvmPropertyUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ChainAspect {

    private static final Logger logger = LogUtil.getLogger();

    @AfterReturning(pointcut=
            "execution(* io.appactive.demo.frontend.service.*.*(..)) || " +
            "execution(* io.appactive.demo.product.service.*.*(..)) || " +
            "execution(* io.appactive.demo.storage.service.*.*(..)) || " +
            "execution(* io.appactive.demo.common.service.springcloud.*.*(..))" ,
            returning = "result")
    public void afterRunning(JoinPoint joinPoint, Object result){
        if (result instanceof ResultHolder){
            ResultHolder resultHolder = (ResultHolder)result;
            resultHolder.addChain(JvmPropertyUtil.getJvmAndEnvValue("appactive.app"),JvmPropertyUtil.getJvmAndEnvValue("appactive.unit"));
            logger.info("ChainAspect: "+resultHolder);
        }
    }
}
