# Confined
A Java library for resiliency in distributed systems

## Introduction

### CircuitBreaker

### BulkHead

### RateLimiter


## Internals


### Resiliency Metadata Table

| Key (String) | Value (JSON) |
|:------------:|:------------:|
| tenant1-ratelimiter:servicea | <JSON_VALUE> |

#### JSON Examples

##### RateLimiter

```
{
    capacity: 100000,
    limitForPeriod: 10,
    limitRefreshPeriodInMillis: 100
    etc: "..."
}
```

##### BulkHead

```
{
    maxConcurrentCalls: 25,
    maxWaitDurationInMillis: 2000,
    etc: "..."
}
```

### Key Structure
Let's consider some examples and understand the key structuring and how to keep it flexible.

##### Bulk Head for Service A
- Key could be structured as either `tenant1-bulkhead:servicea` (if required) in case of multiple tenants or simply as `bulkhead:servicea`.

##### Rate Limiter for Service A (Single Tenant)
- Key could be structured as `ratelimiter:servicea`

##### Rate Limiter for Service A for Tenant T1 (Multiple Tenants)
- Key could be structured as `tenant1-ratelimiter:servicea`


#### Structure 
`Keys` play a big role in operational efficiency `Confined` system.

> The key is required to be `Unique`, `structure` proposed below is to keep **flexibility** for future improvements.

Let's consider an example of `rate limiter`, in case of `distributed rate limiter` we need to maintain `state`. The `state` is maintained against `Key`. Also, in order to keep `mutations` `atomic` and the system `concurrent`, `locks` are taken on `Keys`. 

#### Key (in version 1)
- A key can be 36 characters long.
- A key can use (a-z), (A-Z), (0-9) character set and ":" and "-" as separators.
- This provides flexibility in multi-tenant systems.

> In a multi-tenant system `rate-limiter` would most probably be `tenant` specific. That said, there are chances that all `tenants` or a group of them would be using same `cluster` of `downstream-services`. This, makes `bulk-head`, `circuit-breaker` etc. **common** to all.

##### Example Format

```
    <anything-app-wants>-<PermitType>:<Protected_Resource>
```

### Confined Operations
`Confined` uses [firedb-mysql](https://github.com/yadavanuj/firedb-mysql) and uses `MySQL` as it's storage engine.

Thus, it uses `Packet` semantic to communicate with database. A packet looks like:

```
    private Header[] headers;
    private Operation operation;
    private Serializable data;
    private StatusCodes status;
    private ErrorDetail errorDetail;
```

Here the most important and `required` fields are, `operation` and `data`.

#### Operation
In `version 1` `operation` is a `String` and could have following structure.


```
    <Domain_Resource>:<Operation>
```

This similar to `MVC` where the `Domain_Resource` part helps in identifying the `Controller` and `Operation` helps in identifying the `Action` to be taken.

For example, in order to operate on `Metadata` to `fetch` an entry the `Operation` would be `metadata:get`. Internally, `Confined` uses this information to `select` correct `actor` and performs the action.

#### Data
Here `data` is `JSON` which will be further used by `MySQL` to perform the operation. 