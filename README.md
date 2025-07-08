# Caching Proxy Server (Spring Boot)

A lightweight CLI-based caching proxy server built with Spring Boot. It forwards requests to a specified origin server and caches the responses for subsequent requests, improving response time and reducing redundant network calls.

## Features

* Proxy HTTP GET requests to a configurable origin.
* Cache responses for faster repeat access.
* Expose cache statistics.
* Clear the cache manually.
* CLI-based configuration using `--port` and `--origin`.

---

## 🚀 Getting Started

### Build the Spring Boot Jar File

```bash
gradle bootJar
```

This will generate a JAR file in the `build/libs` directory.

---

## 🖥️ Run the Proxy Server

```bash
java -jar build/libs/caching-proxy.jar --origin=https://dummyjson.com --port=3000
```

* `--origin`: The base URL to which requests will be forwarded.
* `--port`: The port the proxy server will listen on.

---

## 📖 Show Help

```bash
java -jar build/libs/caching-proxy.jar --help
```

Displays usage information and available options.

---

## 📦 Usage

### 🔄 Sending a Request

Proxy any GET request through the server:

```bash
curl -i http://localhost:3000/products
```

* The first request will hit the origin server (`X-Cache: MISS`).
* Subsequent identical requests will return cached responses (`X-Cache: HIT`).

### 🧹 Clearing Cache

```bash
curl -X DELETE http://localhost:3000/clear-cache
```

Clears all cached responses.

### 📊 Viewing Cache Stats

```bash
curl -i http://localhost:3000/stats
```

Returns simple cache statistics (e.g., hits, misses, size).

---

## 🧠 How It Works

* All unmatched routes are intercepted by the `ProxyController`.
* Cached responses are looked up using the request URI + query string.
* If not found, the request is forwarded to the origin, and the response is cached.
* Supports graceful error handling for origin downtime, timeouts, and unexpected failures.
* Uses `RestTemplate` for forwarding and `HttpHeaders` for full response fidelity.

---

## 🛠️ Tech Stack

* Java 24
* Spring Boot 3+
* Gradle
* Picocli (for CLI parsing)
* Lombok

---

## 📁 Project Structure

```
springboot-cache-proxy/
├── controller/
│   └── ProxyController.java
├── cli/
│   └── CachingProxyCommand.java
├── service/
│   └── CacheService.java
├── record/
│   └── CachedResponse.java
├── ...
```

---

## 📃 License

MIT License. Free to use, modify, and distribute.