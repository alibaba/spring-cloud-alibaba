/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.integration.order.mapper;

import com.alibaba.cloud.integration.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import org.springframework.stereotype.Repository;

/**
 * @author TrevorLink
 */
@Mapper
@Repository
public interface OrderMapper {

	@Insert("INSERT INTO `order` (user_id, commodity_code,money,create_time,update_time) VALUES (#{userId}, #{commodityCode},#{money},#{createTime},#{updateTime})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	int saveOrder(Order order);

}
