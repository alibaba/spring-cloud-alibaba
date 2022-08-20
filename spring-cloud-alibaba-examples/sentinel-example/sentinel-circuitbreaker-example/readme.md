# Sentinel Feign Circuit Breaker Example

## Project description

OpenFeign integrates Sentinel circuit breaker implementation

## sample

1. add configuration to config center
	
```yaml
feign:
  circuitbreaker:
    enabled: true # Enable feign circuit breaker support
  sentinel:
    default-rule: default # Default rule name
    rules:
      # Default rule, valid for all feign clients
      default:
        - grade: 2 # Downgrade based on number of exceptions
          count: 1
          timeWindow: 15 # Time to half-open state after downgrade
          statIntervalMs: 1000
          minRequestAmount: 1
      # Only valid for feign client user
      user:
        - grade: 2
          count: 1
          timeWindow: 15
          statIntervalMs: 1000
          minRequestAmount: 1
      # Only valid for the method feignMethod of the feign client user
      # Parentheses are parameter types, separated by multiple commas, such as user#method(boolean,String,Map)
      "[user#feignMethod(boolean)]":
        - grade: 2
          count: 1
          timeWindow: 10
          statIntervalMs: 1000
          minRequestAmount: 1
```
2. start FeignCircuitBreakerApplication

## Verify
Startup project

Verify that the default feign client takes effect.  
First visit http:localhost/test/default/false 2 times (in 1 second)  
and then visit http:localhost/test/default/true, the circuit breaker is open

Verify that the specified feign client takes effect.  
First visit http:localhost/test/feign/false 2 times (in 1 second)  
and then visit http:localhost/test/feign/true, the circuit breaker is open

Verify that the specified method of feign client takes effect.  
First visit http://localhost/test/feignMethod/false 2 times (in 1 second)  
and then visit http://localhost/test/feignMethod/true, the circuit breaker is open

## Rules are dynamically refreshed
Modify the rules of the configuration center, and then access the above interface

