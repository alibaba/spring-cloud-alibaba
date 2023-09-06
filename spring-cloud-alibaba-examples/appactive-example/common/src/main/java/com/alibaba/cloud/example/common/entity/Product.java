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

package com.alibaba.cloud.example.common.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author ChengPu raozihao
 */
@Entity
public class Product implements Serializable {

	@Id
	@Column(nullable = false, columnDefinition = "char(100)")
	private String id;

	@Column(nullable = false, columnDefinition = "char(100)")
	private String name;

	@Column(nullable = false, columnDefinition = "char(100)")
	private String img;

	@Column(nullable = false, columnDefinition = "varchar(2000)")
	private String description;

	private int price;

	private int number;

	public Product() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "Product{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", img='"
				+ img + '\'' + ", description='" + description + '\'' + ", price=" + price
				+ ", number=" + number + '}';
	}

}
