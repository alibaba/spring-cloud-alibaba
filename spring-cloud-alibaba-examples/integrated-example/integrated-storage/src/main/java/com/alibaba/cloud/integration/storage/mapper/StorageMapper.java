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

package com.alibaba.cloud.integration.storage.mapper;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import org.springframework.stereotype.Repository;

/**
 * @author TrevorLink
 */
@Mapper
@Repository
public interface StorageMapper {

	@Select("SELECT `count` FROM storage WHERE commodity_code = #{commodityCode}")
	Integer getStock(@Param("commodityCode") String commodityCode);

	@Update("UPDATE storage SET count = count - #{count},update_time=#{updateTime} WHERE commodity_code = #{commodityCode}")
	int reduceStock(@Param("commodityCode") String commodityCode,
			@Param("count") Integer count, @Param("updateTime") Timestamp updateTime);

}
