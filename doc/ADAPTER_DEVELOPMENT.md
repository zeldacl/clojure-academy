# Protocol Adapter Development Guide

This guide explains how to implement protocol adapters for new Minecraft versions or platforms.

## Overview

Protocol adapters bridge the gap between our protocol-based mod architecture and specific Minecraft/platform implementations. A good adapter:

1. Maintains protocol guarantees
2. Handles platform-specific details
3. Provides clean error handling
4. Ensures thread safety
5. Minimizes performance overhead

## Adapter Implementation Process

### 1. Study Protocol Requirements

First, thoroughly understand the protocol you're implementing:
- Review PROTOCOL_SPEC.md
- Study protocol unit tests
- Note all guarantees that must be maintained

### 2. Research Platform API

For your target platform:
- Identify relevant platform APIs
- Understand threading model
- Note platform-specific limitations
- Research common patterns

### 3. Design Adapter Structure

Create an adapter design that:
- Implements protocol fully
- Handles platform specifics
- Maintains thread safety
- Provides error recovery
- Optimizes performance

### 4. Implementation Steps

1. Create adapter class implementing protocol
2. Add platform-specific fields
3. Implement protocol methods
4. Add error handling
5. Ensure thread safety
6. Add performance optimizations
7. Write integration tests

## Example: Block Protocol Adapter

Here's an example adapter implementation:

```clojure
(defrecord ForgeBlockAdapter [block-instance properties]
  IBlock
  (get-properties [_]
    properties)
    
  (on-placed [_ world pos placer]
    ;; Handle thread safety
    (when (world/is-thread-safe? world)
      (try
        (.onBlockPlaced block-instance world pos placer)
        (catch Exception e
          (log/error e "Error in block placement")))))
          
  (on-broken [_ world pos]
    (when (world/is-thread-safe? world)
      (try
        (.onBlockBroken block-instance world pos)
        (catch Exception e
          (log/error e "Error in block broken")))))
          
  (on-activated [_ world pos player hand]
    (when (world/is-thread-safe? world)
      (try
        (.onBlockActivated block-instance world pos player hand)
        (catch Exception e
          (log/error e "Error in block activation"))))))
```

## Common Adapter Patterns

### Thread Safety

```clojure
(defn with-thread-safety [world f]
  (when (world/is-thread-safe? world)
    (try 
      (f)
      (catch Exception e
        (log/error e "Operation failed")))))
```

### Error Handling

```clojure
(defn safely [f]
  (try
    (f)
    (catch Exception e
      (log/error e "Operation failed")
      nil)))
```

### State Management

```clojure
(defrecord StateManager [state]
  (get-state [this]
    @state)
    
  (update-state [this f]
    (swap! state f))
    
  (with-state-lock [this f]
    (locking state
      (f @state))))
```

## Testing Adapters

### Integration Tests

```clojure
(deftest test-block-adapter
  (let [adapter (->ForgeBlockAdapter block props)]
    
    (testing "Thread safety"
      (let [world (create-test-world)]
        (is (nil? (on-placed adapter 
                            (unsafe-world)
                            [0 0 0]
                            nil)))))
                            
    (testing "Error handling"
      (let [world (create-test-world)]
        (is (nil? (on-activated adapter
                               world
                               [0 0 0]
                               nil
                               nil)))))))
```

### Performance Tests

```clojure
(deftest test-adapter-performance
  (let [adapter (->ForgeBlockAdapter block props)
        world (create-test-world)]
        
    (testing "Operation timing"
      (let [start (System/nanoTime)
            _ (dotimes [_ 1000]
                (on-activated adapter world [0 0 0] nil nil))
            end (System/nanoTime)
            avg (/ (- end start) 1000)]
        (is (< avg 1000000))))))
```

## Best Practices

1. Keep adapters focused and simple
2. Handle all error cases
3. Log meaningful errors
4. Test thread safety thoroughly
5. Measure performance impact
6. Document platform specifics
7. Write integration tests

## Common Issues

1. **Thread Safety Violations**
   - Always check thread safety
   - Use appropriate synchronization
   - Document thread requirements

2. **Memory Leaks**
   - Clean up resources properly
   - Use weak references when needed
   - Test memory usage

3. **Performance Problems**
   - Profile adapter operations
   - Minimize object creation
   - Cache when appropriate

4. **Error Handling**
   - Never swallow exceptions
   - Log with context
   - Maintain system stability

## Additional Resources

- Protocol Specification (PROTOCOL_SPEC.md)
- Test Suite Documentation
- Platform API Documentation
- Development Environment Setup