# Academy Craft - Protocol-Based Minecraft Mod

A Minecraft mod built using a protocol-based architecture in Clojure, providing clean separation between core game logic and Forge/Minecraft implementation details.

## Architecture Overview

The mod is split into three main projects:

- `mcmod`: Core protocols and interfaces defining the mod's behavior
- `acmod`: Implementation of core game logic using the protocols
- `forge_1_16_5`: Forge adapter implementations bridging protocols with Minecraft/Forge

### Protocol System

The protocol-based architecture allows for:

- Clear separation between game logic and Minecraft implementation
- Easy testing through mock implementations
- Flexibility to support multiple Minecraft versions
- Protocol-first design encouraging clean interfaces

Key protocols include:

- Block/Item behavior
- Network messaging
- GUI and Container handling
- Event system
- World and Entity interactions
- Rendering and Particles
- Data generation

## Development

### Prerequisites

- JDK 8
- Clojure CLI tools
- Gradle 7.x

### Building

```bash
./gradlew build
```

### Testing

The project uses a comprehensive testing approach:

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests with coverage
./gradlew check
```

Test reports are available in:
- Unit tests: `build/reports/tests`
- Integration tests: `build/reports/integration-tests`
- Coverage: `build/reports/jacoco`

### Data Generation

Generate mod resources (models, recipes, etc):

```bash
./gradlew :forge_1_16_5:generateModData
```

Generated files will be in `forge_1_16_5/src/generated/resources`.

## Protocol Documentation

### Block Protocol
```clojure
(defprotocol IBlock
  (get-properties [this])
  (on-placed [this world pos placer])
  (on-broken [this world pos])
  (on-activated [this world pos player hand]))
```

### Network Protocol  
```clojure
(defprotocol IPacket
  (encode [this buf])
  (decode [this buf]) 
  (handle [this ctx]))
```

### GUI Protocol
```clojure
(defprotocol IContainer
  (get-slot-count [this])
  (get-stack-in-slot [this slot])
  (set-stack-in-slot [this slot stack]))

(defprotocol IScreen
  (init [this])
  (render [this state])
  (handle-input [this input]))
```

See individual protocol files for full documentation.

## Documentation

- [Adapter Development](doc/ADAPTER_DEVELOPMENT.md)
- [Protocol Specification](doc/PROTOCOL_SPEC.md)
- [Tech System Energy Architecture](doc/TECH_SYSTEM_ENERGY.md)
- [Dependency Rules](doc/DEPENDENCY_RULES.md)
- [Update to 1.14](doc/updateto1.14.md)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes following the protocol architecture
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see LICENSE file for details.
