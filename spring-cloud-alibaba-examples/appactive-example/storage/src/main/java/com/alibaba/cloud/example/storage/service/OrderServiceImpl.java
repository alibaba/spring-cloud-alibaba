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

package com.alibaba.cloud.example.storage.service;

import java.util.Optional;

import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.common.entity.ResultHolder;
import com.alibaba.cloud.example.common.service.OrderDAO;
import com.alibaba.cloud.example.storage.repository.ProductRepository;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderDAO.OrderService {

	private static final Logger logger = LogUtil.getLogger();

	@Autowired
	ProductRepository repository;

	@Value("${appactive.unit}")
	private String unit;

	@Override
	public ResultHolder<String> buy(String rId, String pId, Integer number) {
		String result = null;
		try {
			Optional<Product> op = repository.findById(pId);
			if (op.isPresent()) {
				// todo 扣库存，应该强校验
				Product p = op.get();
				int oldNum = p.getNumber();
				int left = oldNum - number;
				if (left >= 0) {
					p.setNumber(left);
					p = repository.save(p);
					if (p.getNumber() + number != oldNum) {
						result = "storage not consist";
					}
					else {
						result = "success";
					}
				}
				else {
					result = "sold out";
				}
			}
			else {
				result = "no such product";
			}
		}
		catch (Throwable e) {
			result = e.getCause().getCause().getMessage();
		}
		return new ResultHolder<>(result);
	}

}
