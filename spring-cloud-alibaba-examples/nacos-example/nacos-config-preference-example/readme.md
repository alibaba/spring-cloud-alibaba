# Nacos Config Preference

## Project instruction

This project demonstrates how to use config preferences.

## Example

1. Start a Nacos server
add configuration `test.yml`

```yaml
configdata:
  user:
    name: freeman
```

2. Set config preference `spring.cloud.nacos.config.preference=remote`

3. access `localhost`  
You should see a value of `freeman`, because the configuration center configuration is used first.  
Modify the configuration to `spring.cloud.nacos.config.preference=local` then the value should be `aa`.