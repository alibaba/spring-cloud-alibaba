curl --location 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation' \
--header 'Authorization: Bearer sk-a3d73b1709bf4a178c28ed7c8b3b5a54' \
--header 'Content-Type: application/json' \
--data '{
    "model": "qwen-turbo",
    "input":{
        "messages":[
            {
                "role": "system",
                "content": "You are a helpful assistant."
            },
            {
                "role": "user",
                "content": "Tell me a laugh"
            }
        ]
    },
    "parameters": {
        "result_format": "message"
    }
}'