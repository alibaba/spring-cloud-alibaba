# spring-cloud-alibaba-rag-helper

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 功能简介

`spring-cloud-alibaba-rag-helper` 是一个辅助工具，旨在帮助用户通过自然语言查询快速获取 Spring Cloud Alibaba 相关文档中的信息。该工具的主要特点包括：

- **文档向量化存储**：将项目中所有的 `.md` 文档转换为向量形式并存储在 Milvus 向量数据库中。
- **检索增强（RAG）**：结合大模型进行检索增强，提供更加准确的回答，使得用户能够以自然语言提问并获得基于官方文档的最佳答案。

## 使用场景

- 开发者希望快速查找 Spring Cloud Alibaba 的相关文档信息。
- 希望通过自然语言处理(NLP)技术提升对文档内容的理解和检索效率。

## 技术栈

- **Python**：用于实现文档解析、向量化以及与相似度检索。
- **Milvus**：高效的向量数据库，用于存储文档向量。
- **Spring Boot**：后端服务框架，提供 API 接口。

## 快速开始

### 安装依赖

确保你的环境中已经安装了 Python 3.9+ 和 Java 17+。
