#
#  Copyright 2023-2024 the original author or authors.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

# HTTP call example
curl --location 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation' \
--header 'Authorization: Bearer sk-a3d73b1709bf4a178c28ed7c8b3b5a45' \
--header 'Content-Type: application/json' \
--data '{
    "model": "qwen-turbo",
    "input":{
        "messages":[
          {
                "role": "system",
                "content": "You are a helpful AI assistant.You are an AI assistant that helps people find information.Your name is yuluo.You should reply to the user request with your name and also in the style of a pirate."
          },
          {
              "role": "user",
              "content": "who are you?"
          }
        ]
    },
    "parameters": {
        "result_format": "message"
    }
}'