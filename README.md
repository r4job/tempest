# Tempest: Machine Learning-based Cache Preheating System

**Tempest** is an intelligent caching infrastructure designed to proactively warm caches based on learned traffic patterns. It combines **metric emission**, **aggregation**, and **prediction** components to reduce cold-start latency and offload backend systems under burst traffic.

---

## 🌪️ Overview

Tempest aims to solve the cold cache problem by forecasting which items will be queried in the near future, and warming those into cache **before** they're needed.

The system consists of:

- 🔌 **Metric Emission Layer**: Collects and forwards structured click/view events via pluggable backends
- 📊 **Metric Aggregation Layer**: Aggregates metrics by time windows (hourly, daily, etc.) to form time series
- ⏱️ **Prediction Engine**: Uses time-series forecasting and online learning to predict future hot keys
- 🔥 **Cache Warmer**: Loads predicted items into cache using appropriate eviction and TTL policies

> The emission layer is complete. Aggregation and prediction layers are under development.

---

## 🧠 Background

In content-rich platforms (e.g., e-commerce, media, search), item access patterns are **bursty** and **seasonal**. Cold cache misses can result in:

- Latency spikes
- Overloaded databases
- Poor user experience

Traditional solutions (e.g., TTL tuning or LRU policies) are reactive. Tempest takes a **proactive** approach—**predicting and warming** frequently accessed items **before** demand arises.

---

## ❗ Problem Statement

Real-world caching systems face several challenges:

- ❌ Cold starts during sudden popularity surges (e.g., flash sales)
- ❌ Inefficient TTL-based eviction misses slow-rising items
- ❌ Difficulty modeling diverse traffic patterns per item
- ❌ Metrics may be lost due to network failure or service crashes

---

## ✅ Proposed Solution

Tempest addresses these via a modular, extensible pipeline:

1. **Metric Emission** (done)
    - Collects user interaction data (item, timestamp, count)
    - Durable, async, and retry-capable
2. **Metric Aggregation** (WIP)
    - Aggregates metrics into per-item time series
    - Resamples and cleans data for modeling
3. **Prediction Engine** (WIP)
    - Uses online learning (e.g., Vowpal Wabbit) or time-series models to forecast traffic
    - Selects top-K hot items
4. **Cache Warmer** (WIP)
    - Loads forecasted keys into the target cache
    - Supports Redis and other distributed caches

---

## 📦 Features

- ✅ Durable metric emitter (file-based recovery)
- ✅ Asynchronous and batched delivery
- ✅ Pluggable backend emitters (Kafka, HTTP, gRPC, RabbitMQ, etc.)
- ✅ MetricEmitterBuilder & MetricEmitterFactory for easy setup
- 🔜 Time-based aggregation & prediction
- 🔜 Configurable cache warming strategies

---

## 📤 Metric Emission Quickstart

```java
MetricEvent event = new MetricEvent("Product", "item123", System.currentTimeMillis(), 1);

MetricEmitter emitter = MetricEmitterBuilder.emitter(new KafkaMetricEmitter("localhost:9092", "metrics"))
    .withRetry(true, 3, 100)
    .withDurability(true, new File("metrics"))
    .withAsync(true)
    .withBatch(true, 5)
    .build();

emitter.emit(event);
```

---

## 🧪 Testing

```bash
./gradlew test
```

- Unit tests are available for all emitters and core modules
- Integration tests coming soon for aggregation and warming pipelines

---

## 📁 Project Structure

```
com.tempest
├── metric               # Emission interface & builder
│   ├── impl             # Concrete emitters (Kafka, HTTP, etc.)
│   └── durability       # File-based durability store
├── aggregation          # (WIP) Time window resampling
├── predictor            # (WIP) Forecasting and scoring
├── warmer               # (WIP) Cache preheater interface
├── config               # YAML/JSON config objects
├── grpc                 # Grpc connector
```

---

## 📜 License

[MIT License](https://github.com/r4job/tempest?tab=MIT-1-ov-file)

---

## 🛤️ Roadmap

- [x] MetricEmitter with durability/retry/backpressure
- [ ] Metric aggregation engine (rolling windows, grouping)
- [ ] Online learner for top-K prediction (VW, River)
- [ ] Cache warming executor for Redis
- [ ] Observability dashboard (optional)

---

## 🤝 Contributions

Contributions and discussions are welcomed!
