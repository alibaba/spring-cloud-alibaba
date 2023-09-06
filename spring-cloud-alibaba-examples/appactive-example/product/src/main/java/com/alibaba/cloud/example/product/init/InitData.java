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

package com.alibaba.cloud.example.product.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.cloud.example.common.entity.Product;
import com.alibaba.cloud.example.product.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static com.alibaba.cloud.example.common.Constants.CENTER_FLAG;

@Component
public class InitData implements ApplicationRunner {

	@Resource
	ProductRepository productRepository;

	@Value("${appactive.unit}")
	private String unit;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		if (!CENTER_FLAG.equals(unit)) {
			return;
		}

		List<Product> products = new ArrayList<>(4);
		Product p1 = new Product();
		p1.setId("12");
		p1.setName("书包");
		p1.setImg("/img/backpack.png");
		p1.setDescription("好用的书包");
		p1.setPrice(300);
		p1.setNumber(10);
		products.add(p1);

		Product p2 = new Product();
		p2.setId("14");
		p2.setName("球拍");
		p2.setImg("/img/badminton.png");
		p2.setDescription("好用的球拍");
		p2.setPrice(200);
		p2.setNumber(20);
		products.add(p2);

		Product p3 = new Product();
		p3.setId("16");
		p3.setName("键盘");
		p3.setImg("/img/keyboard.png");
		p3.setDescription("好用的键盘");
		p3.setPrice(800);
		p3.setNumber(50);
		products.add(p3);

		Product p4 = new Product();
		p4.setId("18");
		p4.setName("茶杯");
		p4.setImg("/img/cup.png");
		p4.setDescription("好用的茶杯");
		p4.setPrice(100);
		p4.setNumber(60);
		products.add(p4);

		try {
			productRepository.deleteAll();
			productRepository.saveAll(products);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
