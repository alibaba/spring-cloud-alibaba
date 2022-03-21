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

add configuration `test2.yml`
```yaml
dev:
  age: 22
```

2. Set configuration preference

Set default configuration preference
```yaml
spring:
  cloud:
    nacos:
      config:
        preference: remote
```

Specify configuration (test 2.yml) to set configuration preference
```yaml
spring:
  config:
    import:
      - optional:nacos:test.yml
      - optional:nacos:test2.yml?preference=local
```

3. Verify 
Access `localhost`, you should see the value of `freeman: 20`, because `name` uses the configuration center configuration first, and `age` uses the local configuration first.