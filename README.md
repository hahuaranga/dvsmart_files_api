# dvsmart_files_api

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
  - [Posición en el Ecosistema](#posición-en-el-ecosistema)
  - [Diagrama de Componentes](#diagrama-de-componentes)
  - [Arquitectura Hexagonal](#arquitectura-hexagonal)
  - [Flujo de Procesamiento](#flujo-de-procesamiento)
  - [Técnicas de Optimización](#técnicas-de-optimización)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Setup](#-instalación-y-setup)
- [Guía Completa de Configuración](#-guía-completa-de-configuración)
- [Configuración de Alto Rendimiento](#-configuración-de-alto-rendimiento)
- [Uso y API](#-uso-y-api)
- [Monitorización y Observabilidad](#-monitorización-y-observabilidad)
- [Troubleshooting](#-troubleshooting)
- [Mantenimiento y Testing](#-mantenimiento-y-testing)
- [Referencias](#-referencias)
- [Soporte y Contacto](#-soporte-y-contacto)

---

## 🎯 Descripción General

**dvsmart_files_api** es un microservicio REST de alto rendimiento diseñado para búsqueda, visualización y descarga de archivos PDF almacenados en el servidor SFTP destino. El sistema consume el catálogo de archivos indexados en MongoDB (colección `files_index`) y accede a los archivos organizados mediante hash partitioning por `dvsmart_reorganization_api`.

### Casos de Uso Principales

- **Búsqueda avanzada**: Localización de archivos por nombre, tipo de documento, cliente, año y mes
- **Visualización de metadata**: Consulta de información completa de archivos indexados
- **Descarga eficiente**: Streaming de archivos PDF desde rutas hash-partitioned (acceso O(1))
- **Preview de documentos**: Generación de thumbnails de páginas PDF
- **Estadísticas agregadas**: Métricas de uso y distribución del catálogo

### Características Clave

✅ **Pool de Conexiones SFTP Lazy**: Conexiones creadas bajo demanda, validadas antes de uso  
✅ **Streaming Zero-Copy**: Descarga directa sin cargar archivos en memoria  
✅ **Acceso O(1) Hash-Partitioned**: Rutas pre-calculadas para acceso instantáneo  
✅ **Preview PDF con PDFBox**: Renderizado de páginas como imágenes PNG/JPEG  
✅ **Búsqueda Paginada**: Consultas eficientes con filtros múltiples  
✅ **OpenAPI/Swagger**: Documentación interactiva de API  
✅ **Arquitectura Hexagonal**: Separación clara de responsabilidades  

---

## 🛠 Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje base con soporte LTS |
| **Spring Boot** | 4.0.1 | Framework de aplicación |
| **Spring Data MongoDB** | - | Acceso a catálogo de archivos |
| **Spring Integration** | - | Integración con sistemas externos (SFTP) |
| **Apache PDFBox** | 3.0.3 | Renderizado de PDFs para preview |
| **Apache Commons Pool2** | - | Gestión del pool de conexiones SFTP |
| **SSHJ** | 0.38.0 | Cliente SFTP nativo |
| **SpringDoc OpenAPI** | 2.7.0 | Documentación de API |
| **Lombok** | - | Reducción de boilerplate |
| **Maven** | 3.8+ | Gestión de dependencias y build |

---

## 🏗 Arquitectura del Sistema

### Posición en el Ecosistema

```mermaid
flowchart TB
    subgraph ORIGEN["🗄️ SFTP ORIGEN<br/>(Desorganizado)"]
        SO["/disorganized_data/<br/>millones de archivos"]
    end

    subgraph DESTINO["🗄️ SFTP DESTINO<br/>(Hash-Partitioned)"]
        SD["/organized_data/<br/>a1/b2/c3/archivo.pdf"]
    end

    subgraph MONGO["🍃 MongoDB"]
        FI[(files_index<br/>11M+ documentos)]
    end

    subgraph APIS["📦 Microservicios DVSmart"]
        direction TB
        
        subgraph API1["dvsmart_indexing_api"]
            IDX["📋 Indexar archivos<br/>Crear catálogo en MongoDB"]
        end
        
        subgraph API2["dvsmart_reorganization_api"]
            REORG["📂 Reorganizar archivos<br/>Hash Partitioning"]
        end
        
        subgraph API3["dvsmart_files_api ⭐"]
            FILES["🔍 Buscar archivos<br/>👁️ Visualizar metadata<br/>📄 Preview PDF<br/>⬇️ Descargar"]
        end
    end

    subgraph CLIENTS["👥 Clientes"]
        WEB["🖥️ Web App"]
        MOBILE["📱 Mobile App"]
        EXTERNAL["⚙️ Sistemas Externos"]
    end

    SO -->|"1️⃣ Escanea y lee metadata"| IDX
    IDX -->|"2️⃣ Registra en catálogo"| FI
    
    FI -->|"3️⃣ Consulta pendientes"| REORG
    SO -->|"4️⃣ Lee archivo original"| REORG
    REORG -->|"5️⃣ Escribe con hash-partition"| SD
    REORG -->|"6️⃣ Actualiza estado"| FI
    
    FI -->|"7️⃣ Busca y consulta metadata"| FILES
    SD -->|"8️⃣ Streaming de archivos"| FILES
    
    FILES -->|"9️⃣ API REST"| WEB
    FILES -->|"9️⃣ API REST"| MOBILE
    FILES -->|"9️⃣ API REST"| EXTERNAL

    style API3 fill:#90EE90,stroke:#228B22,stroke-width:3px
    style SD fill:#87CEEB,stroke:#4169E1,stroke-width:2px
    style FI fill:#FFE4B5,stroke:#FF8C00,stroke-width:2px
```

### Diagrama de Componentes

```mermaid
graph TB
    subgraph "Cliente"
        API[REST Client / Browser]
    end

    subgraph "Capa de Entrada"
        FC[FileController<br/>/api/files/*]
        MC[MonitoringController<br/>/api/monitoring/*]
        GEH[GlobalExceptionHandler]
    end

    subgraph "Casos de Uso"
        SFU[SearchFilesUseCase]
        GFU[GetFileUseCase]
        DFU[DownloadFileUseCase]
        PFU[PreviewFileUseCase]
        GSU[GetStatisticsUseCase]
    end

    subgraph "Servicios de Dominio"
        FSS[FileSearchService]
        FDS[FileDownloadService]
        PPS[PdfPreviewService]
    end

    subgraph "Puertos de Salida"
        FMP[FileMetadataPort]
        FCP[FileContentPort]
    end

    subgraph "Adaptadores de Salida"
        FMMA[FileMetadataMongoAdapter]
        SFCA[SftpFileContentAdapter]
    end

    subgraph "Infraestructura"
        POOL[CustomLazySftpSessionFactory]
        MONITOR[SftpPoolMonitor]
    end

    subgraph "Sistemas Externos"
        SFTP[Servidor SFTP Destino<br/>/organized_data]
        MONGO[(MongoDB<br/>files_index)]
    end

    API -->|HTTP Request| FC
    API -->|HTTP Request| MC
    
    FC --> SFU
    FC --> GFU
    FC --> DFU
    FC --> PFU
    FC --> GSU
    
    SFU --> FSS
    GFU --> FSS
    GSU --> FSS
    DFU --> FDS
    PFU --> PPS
    PPS --> FDS

    FSS --> FMP
    FDS --> FMP
    FDS --> FCP

    FMP --> FMMA
    FCP --> SFCA

    FMMA --> MONGO
    SFCA --> POOL
    POOL --> SFTP
    MC --> MONITOR
    MONITOR --> POOL

    style FC fill:#87CEEB
    style FSS fill:#90EE90
    style FDS fill:#90EE90
    style PPS fill:#90EE90
    style POOL fill:#FFB6C1
```

### Arquitectura Hexagonal

```mermaid
flowchart TB
    subgraph EXTERNAL_IN["🌐 Mundo Exterior (Entrada)"]
        HTTP_CLIENT["HTTP Clients<br/>(Browser, Apps, APIs)"]
    end

    subgraph ADAPTERS_IN["📥 ADAPTADORES DE ENTRADA<br/>(Driving Adapters)"]
        direction TB
        FC["FileController"]
        MC["MonitoringController"]
        GEH["GlobalExceptionHandler"]
        
        subgraph DTOS["DTOs"]
            FSR["FileSearchRequest"]
            FR["FileResponse"]
            PSR["PagedSearchResponse"]
            ER["ErrorResponse"]
        end
    end

    subgraph PORTS_IN["🚪 PUERTOS DE ENTRADA<br/>(Input Ports / Use Cases)"]
        SFU["«interface»<br/>SearchFilesUseCase"]
        GFU["«interface»<br/>GetFileUseCase"]
        DFU["«interface»<br/>DownloadFileUseCase"]
        PFU["«interface»<br/>PreviewFileUseCase"]
        GSU["«interface»<br/>GetStatisticsUseCase"]
    end

    subgraph DOMAIN["🎯 DOMINIO<br/>(Núcleo de Negocio)"]
        subgraph MODELS["Modelos"]
            FM["FileMetadata"]
            SC["SearchCriteria"]
            PR["PagedResult"]
            FS["FileStatistics"]
        end
        
        subgraph SERVICES["Servicios"]
            FSS["FileSearchService"]
            FDS["FileDownloadService"]
            PPS["PdfPreviewService"]
        end
        
        subgraph EXCEPTIONS["Excepciones"]
            FNF["FileNotFoundException"]
            FNA["FileNotAvailableException"]
            PGE["PreviewGenerationException"]
            SCE["SftpConnectionException"]
        end
    end

    subgraph PORTS_OUT["🚪 PUERTOS DE SALIDA<br/>(Output Ports)"]
        FMP["«interface»<br/>FileMetadataPort"]
        FCP["«interface»<br/>FileContentPort"]
    end

    subgraph ADAPTERS_OUT["📤 ADAPTADORES DE SALIDA<br/>(Driven Adapters)"]
        FMMA["FileMetadataMongoAdapter"]
        SFCA["SftpFileContentAdapter"]
        
        subgraph INFRA["Infraestructura"]
            CLSF["CustomLazySftpSessionFactory"]
            FIR["FileIndexMongoRepository"]
            FMM["FileMetadataMapper"]
        end
    end

    subgraph EXTERNAL_OUT["🗄️ Mundo Exterior (Salida)"]
        MONGODB[("MongoDB<br/>files_index")]
        SFTP_DEST[("SFTP Destino<br/>/organized_data")]
    end

    HTTP_CLIENT --> FC
    HTTP_CLIENT --> MC
    FC --> SFU
    FC --> GFU
    FC --> DFU
    FC --> PFU
    FC --> GSU
    
    SFU --> FSS
    GFU --> FSS
    GSU --> FSS
    DFU --> FDS
    PFU --> PPS
    
    FSS --> FMP
    FDS --> FMP
    FDS --> FCP
    PPS --> FCP
    
    FMP --> FMMA
    FCP --> SFCA
    
    FMMA --> MONGODB
    SFCA --> SFTP_DEST

    style DOMAIN fill:#E8F5E9,stroke:#2E7D32,stroke-width:2px
    style PORTS_IN fill:#E3F2FD,stroke:#1565C0,stroke-width:1px
    style PORTS_OUT fill:#E3F2FD,stroke:#1565C0,stroke-width:1px
    style ADAPTERS_IN fill:#FFF3E0,stroke:#EF6C00,stroke-width:1px
    style ADAPTERS_OUT fill:#FCE4EC,stroke:#C2185B,stroke-width:1px
```

### Flujo de Procesamiento

#### Diagrama de Secuencia: Descarga de Archivo

```mermaid
sequenceDiagram
    autonumber
    
    participant Client as 🌐 Cliente
    participant FC as 📥 FileController
    participant DFU as 🎯 DownloadFileUseCase
    participant FDS as ⚙️ FileDownloadService
    participant FMP as 🚪 FileMetadataPort
    participant FMMA as 🍃 MongoAdapter
    participant MONGO as 🗄️ MongoDB
    participant FCP as 🚪 FileContentPort
    participant SFCA as 📁 SftpAdapter
    participant POOL as 🔌 SessionPool
    participant SFTP as 🗄️ SFTP Destino

    Client->>FC: GET /api/files/{idUnico}/download
    
    FC->>DFU: execute(idUnico, outputStream)
    DFU->>FDS: downloadFile(idUnico, outputStream)
    
    Note over FDS,MONGO: 1️⃣ Buscar metadata en MongoDB
    FDS->>FMP: findByIdUnico(idUnico)
    FMP->>FMMA: findByIdUnico(idUnico)
    FMMA->>MONGO: db.files_index.findOne({idUnico})
    MONGO-->>FMMA: FileIndexDocument
    FMMA-->>FMP: FileMetadata
    FMP-->>FDS: FileMetadata
    
    Note over FDS: 2️⃣ Validar disponibilidad
    FDS->>FDS: isAvailableForDownload()?
    
    alt reorg_status != COMPLETED
        FDS-->>FC: ❌ FileNotAvailableException
        FC-->>Client: 409 Conflict
    end
    
    Note over FDS,SFTP: 3️⃣ Streaming desde SFTP Destino
    FDS->>FCP: streamFileTo(destinationPath, outputStream)
    FCP->>SFCA: streamFileTo(path, output)
    
    SFCA->>POOL: getSession()
    POOL-->>SFCA: SftpSession (validada)
    
    Note right of SFCA: Ruta hash-partitioned:<br/>/organized_data/a1/b2/c3/file.pdf<br/>⚡ Acceso O(1)
    
    SFCA->>SFTP: session.readRaw(path)
    
    loop Streaming por chunks (8KB)
        SFTP-->>SFCA: byte[] chunk
        SFCA-->>Client: write(chunk)
    end
    
    SFCA->>POOL: returnSession()
    
    FDS-->>FC: FileMetadata
    
    Note over FC,Client: 4️⃣ Headers de respuesta
    FC->>FC: Set Content-Type, Content-Disposition
    FC-->>Client: 200 OK + PDF Stream

    Note over Client: ✅ Descarga completada
```

#### Diagrama de Secuencia: Búsqueda de Archivos

```mermaid
sequenceDiagram
    autonumber
    
    participant Client as 🌐 Cliente
    participant FC as 📥 FileController
    participant SCM as 🔄 SearchCriteriaMapper
    participant SFU as 🎯 SearchFilesUseCase
    participant FSS as ⚙️ FileSearchService
    participant FMP as 🚪 FileMetadataPort
    participant FMMA as 🍃 MongoAdapter
    participant MONGO as 🗄️ MongoDB

    Client->>FC: GET /api/files/search?q=factura&tipoDocumento=FACTURA&page=0&size=20
    
    FC->>SCM: toDomain(FileSearchRequest)
    SCM-->>FC: SearchCriteria
    
    FC->>SFU: execute(criteria)
    SFU->>FSS: search(criteria)
    
    Note over FSS,MONGO: Construir query con filtros
    FSS->>FMP: search(criteria)
    FMP->>FMMA: search(criteria)
    
    FMMA->>FMMA: buildSearchQuery(criteria)
    Note right of FMMA: Criterios:<br/>- reorg_status = COMPLETED<br/>- fileName regex "factura"<br/>- business_tipoDocumento = "FACTURA"
    
    FMMA->>MONGO: count(query)
    MONGO-->>FMMA: totalElements
    
    FMMA->>MONGO: find(query).skip().limit().sort()
    MONGO-->>FMMA: List<FileIndexDocument>
    
    FMMA->>FMMA: map(toDomain)
    FMMA-->>FMP: PagedResult<FileMetadata>
    FMP-->>FSS: PagedResult
    FSS-->>SFU: PagedResult
    SFU-->>FC: PagedResult
    
    FC->>FC: PagedSearchResponse.fromDomain()
    FC-->>Client: 200 OK + JSON Response
```

### Técnicas de Optimización

#### 1. Pool de Conexiones SFTP Lazy

```mermaid
stateDiagram-v2
    [*] --> Idle: Pool inicializado<br/>(initial-size=0)
    
    Idle --> Creating: getSession() llamado
    Creating --> Active: Nueva sesión creada
    Active --> Validating: testOnBorrow=true
    Validating --> InUse: ✅ Validación OK
    Validating --> Destroyed: ❌ Validación falla
    
    InUse --> Returning: close() llamado
    Returning --> Idle: Devuelta al pool
    
    Idle --> Evicting: Eviction timer<br/>(cada 60s)
    Evicting --> Destroyed: idle > 5 min
    
    Destroyed --> [*]
    
    note right of Validating
        Valida ejecutando:
        session.list(baseDir)
    end note
    
    note right of InUse
        Operaciones:
        - readRaw()
        - read()
        - exists()
    end note
```

**Configuración:**
```properties
sftp.dest.pool.lazy-init=true
sftp.dest.pool.initial-size=0
sftp.dest.pool.max-size=20
sftp.dest.pool.test-on-borrow=true
sftp.dest.pool.min-evictable-idle-time-millis=300000
```

#### 2. Streaming Zero-Copy

```java
// ❌ MALO: Cargar archivo completo en memoria
byte[] content = session.read(path);  // OOM para archivos grandes!

// ✅ BUENO: Streaming directo al response
public void streamFileTo(String path, OutputStream output) {
    try (InputStream is = session.readRaw(path)) {
        byte[] buffer = new byte[8192];  // 8KB buffer
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
```

#### 3. Acceso O(1) con Hash Partitioning

```mermaid
flowchart LR
    subgraph SIN_HASH["❌ Sin Hash Partitioning"]
        DIR1["/data/"]
        DIR1 --> F1["archivo1.pdf"]
        DIR1 --> F2["archivo2.pdf"]
        DIR1 --> FN["... 11M archivos"]
        
        NOTE1["⚠️ Listado directorio: O(n)<br/>Tiempo: segundos a minutos"]
    end
    
    subgraph CON_HASH["✅ Con Hash Partitioning"]
        DIR2["/organized_data/"]
        DIR2 --> H1["a1/"]
        DIR2 --> H2["b2/"]
        DIR2 --> HN["..."]
        
        H1 --> H1A["b2/"]
        H1A --> H1B["c3/"]
        H1B --> F3["archivo.pdf"]
        
        NOTE2["✅ Acceso directo: O(1)<br/>Tiempo: milisegundos"]
    end

    style CON_HASH fill:#90EE90
    style SIN_HASH fill:#FFB6C1
```

**Algoritmo:**
```java
// idUnico = SHA-256(sourcePath) = "a1b2c3d4e5f6..."
// Extracción: a1 / b2 / c3
String path = baseDir + "/" + 
              idUnico.substring(0,2) + "/" +  // a1
              idUnico.substring(2,4) + "/" +  // b2
              idUnico.substring(4,6) + "/" +  // c3
              fileName;
// Resultado: /organized_data/a1/b2/c3/documento.pdf
```

---

## 📦 Requisitos Previos

### Software Requerido

- **JDK 21** (OpenJDK o Oracle)
- **Maven 3.8+**
- **MongoDB 5.0+** (con colección `files_index` poblada)
- **Servidor SFTP Destino** (con archivos reorganizados)

### Dependencias del Ecosistema

| Componente | Estado Requerido | Descripción |
|------------|------------------|-------------|
| `dvsmart_indexing_api` | ✅ Ejecutado | Pobló `files_index` en MongoDB |
| `dvsmart_reorganization_api` | ✅ Ejecutado | Organizó archivos en SFTP destino |
| MongoDB `files_index` | ✅ Con datos | Catálogo de archivos indexados |
| SFTP Destino | ✅ Accesible | `/organized_data` con estructura hash |

### Recursos Mínimos

**Desarrollo:**
- RAM: 2 GB
- CPU: 2 cores
- Disco: 1 GB

**Producción:**
- RAM: 4-8 GB
- CPU: 4 cores
- Disco: 2 GB (logs)

---

## 🚀 Instalación y Setup

### Compilación del Proyecto

```bash
# Clonar repositorio
git clone <repository-url>
cd dvsmart_files_api

# Verificar Maven
mvn -version

# Limpiar y compilar (skip tests)
mvn clean package -DskipTests

# Compilar y ejecutar tests
mvn clean install
```

**Artefacto generado:** `target/dvsmart_files_api.jar`

### Aplicar Licencias (CopyRight Headers)

```bash
# Aplicar headers a todos los archivos .java
mvn license:format

# Verificar headers
mvn license:check
```

### Verificar Prerequisitos

**MongoDB (files_index debe existir):**

```bash
# Conectar a MongoDB
mongo localhost:30000/dvsmart-ms -u dvsmart_user -p eoQQqfTyMd --authenticationDatabase dvsmart-ms

# Verificar colección
db.files_index.countDocuments()
# Debe retornar > 0

# Verificar archivos disponibles (reorganizados)
db.files_index.countDocuments({reorg_status: "COMPLETED"})
```

**Servidor SFTP Destino:**

```bash
# Test de conectividad
sftp -P 30001 sftpdestinationuser@localhost

# Verificar estructura hash-partitioned
ls /organized_data/
# Debe mostrar directorios como: a1/, b2/, c3/, etc.
```

### Ejecución Local

```bash
# Modo development
mvn spring-boot:run

# O usando el JAR compilado
java -jar target/dvsmart_files_api.jar

# Con perfil específico
java -jar target/dvsmart_files_api.jar --spring.profiles.active=prod

# Sobreescribir propiedades
java -jar target/dvsmart_files_api.jar \
  --server.port=9090 \
  --sftp.dest.host=192.168.1.100
```

La aplicación estará disponible en:
```
http://localhost:8080/dvsmart_files_api
```

**Swagger UI:**
```
http://localhost:8080/dvsmart_files_api/swagger-ui.html
```

---

## ⚙️ Guía Completa de Configuración

### Archivo: `application.properties`

#### Configuración Base

```properties
# Aplicación
spring.application.name=dvsmart-files-api
server.servlet.context-path=/dvsmart_files_api
server.port=8080
server.shutdown=graceful
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `spring.application.name` | `dvsmart-files-api` | Nombre para métricas y logs |
| `server.servlet.context-path` | `/dvsmart_files_api` | Context path base |
| `server.port` | `8080` | Puerto HTTP |
| `server.shutdown` | `graceful` | Espera requests activos antes de cerrar |

#### MongoDB

```properties
spring.data.mongodb.uri=mongodb://dvsmart_user:eoQQqfTyMd@localhost:30000/dvsmart-ms?authSource=dvsmart-ms

# ⚠️ CRÍTICO: Desactivar auto-creación de índices
# Los índices ya fueron creados por dvsmart_indexing_api
spring.data.mongodb.auto-index-creation=false
```

**Colección utilizada:**

| Colección | Propósito | Creada por |
|-----------|-----------|------------|
| `files_index` | Catálogo de archivos PDF | `dvsmart_indexing_api` |

#### Servidor SFTP Destino

```properties
# ⭐ Este es el servidor donde reorganization_api colocó los archivos
sftp.dest.host=localhost
sftp.dest.port=30001
sftp.dest.user=sftpdestinationuser
sftp.dest.password=securepass
sftp.dest.base-dir=/organized_data
sftp.dest.timeout=30000
sftp.dest.known-hosts-file=
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `sftp.dest.host` | `localhost` | Host del servidor SFTP destino |
| `sftp.dest.port` | `30001` | Puerto SFTP |
| `sftp.dest.base-dir` | `/organized_data` | Directorio base con estructura hash |

#### Pool de Conexiones SFTP

```properties
# Pool lazy - conexiones bajo demanda
sftp.dest.pool.lazy-init=true
sftp.dest.pool.initial-size=0
sftp.dest.pool.max-size=20
sftp.dest.pool.min-idle=0
sftp.dest.pool.max-wait-millis=30000

# Validación
sftp.dest.pool.test-on-borrow=true
sftp.dest.pool.test-while-idle=true

# Eviction (limpieza de idle)
sftp.dest.pool.time-between-eviction-runs-millis=60000
sftp.dest.pool.min-evictable-idle-time-millis=300000
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `lazy-init` | `true` | No crear conexiones al inicio |
| `max-size` | `20` | Máximo de conexiones simultáneas |
| `test-on-borrow` | `true` | **CRÍTICO:** Validar antes de usar |
| `min-evictable-idle-time-millis` | `300000` | 5 min idle antes de cerrar |

#### Configuración de la API de Archivos

```properties
# Búsqueda
files.search.default-page-size=20
files.search.max-page-size=100

# Descarga
files.download.buffer-size=8192

# Preview PDF
files.preview.default-width=300
files.preview.default-height=400
files.preview.default-format=png
files.preview.dpi=150
```

| Propiedad | Valor | Descripción |
|-----------|-------|-------------|
| `default-page-size` | `20` | Tamaño de página por defecto |
| `max-page-size` | `100` | Máximo elementos por página |
| `buffer-size` | `8192` | Buffer de 8KB para streaming |
| `dpi` | `150` | Resolución para preview PDF |

#### Compresión HTTP

```properties
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
server.compression.min-response-size=1024
```

#### Actuator / Monitorización

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.health.mongo.enabled=true

management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
```

#### Logging

```properties
# Niveles generales
logging.level.root=INFO
logging.level.com.indra.minsait.dvsmart.files=DEBUG

# Componentes específicos
logging.level.org.springframework.data.mongodb=INFO
logging.level.org.springframework.integration.sftp=INFO
logging.level.com.jcraft.jsch=WARN

# Patrón
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

#### OpenAPI / Swagger

```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

---

## 🔥 Configuración de Alto Rendimiento

### Tuning de la JVM

```bash
# Variables de entorno
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

java $JAVA_OPTS -jar target/dvsmart_files_api.jar
```

| Flag | Valor | Propósito |
|------|-------|-----------|
| `-Xms2g` | Heap inicial 2GB | Evita resizing |
| `-Xmx4g` | Heap máximo 4GB | Suficiente para streaming |
| `-XX:+UseG1GC` | G1 GC | Baja latencia |

### Tuning para Alta Concurrencia

```properties
# Pool SFTP más grande
sftp.dest.pool.max-size=50

# Buffer más grande para streaming
files.download.buffer-size=16384

# Mayor timeout para archivos grandes
sftp.dest.timeout=60000
```

### Configuraciones por Entorno

```properties
# 🔹 DESARROLLO
sftp.dest.pool.max-size=5
files.search.max-page-size=50

# 🔹 PRODUCCIÓN ESTÁNDAR
sftp.dest.pool.max-size=20
files.search.max-page-size=100

# 🔹 ALTO RENDIMIENTO
sftp.dest.pool.max-size=50
files.download.buffer-size=32768
```

---

## 📡 Uso y API

### Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/files/search` | Búsqueda con filtros |
| `GET` | `/api/files/{idUnico}` | Obtener metadata |
| `GET` | `/api/files/{idUnico}/download` | Descargar PDF |
| `GET` | `/api/files/{idUnico}/view` | Ver PDF en navegador |
| `GET` | `/api/files/{idUnico}/preview` | Preview como imagen |
| `GET` | `/api/files/stats` | Estadísticas |
| `GET` | `/api/monitoring/sftp-pool` | Estado del pool |
| `GET` | `/api/monitoring/sftp-pool/health` | Health del pool |

### 🔵 Búsqueda de Archivos

```http
GET /dvsmart_files_api/api/files/search
```

**Query Parameters:**

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `q` | String | No | Búsqueda por nombre (parcial, case-insensitive) |
| `tipoDocumento` | String | No | Filtro: FACTURA, CONTRATO, RECIBO, etc. |
| `codigoCliente` | String | No | Filtro por código de cliente |
| `anio` | Integer | No | Filtro por año |
| `mes` | Integer | No | Filtro por mes (1-12) |
| `page` | Integer | No | Número de página (default: 0) |
| `size` | Integer | No | Tamaño de página (default: 20, max: 100) |
| `sort` | String | No | Campo: fileName, fileSize, lastModificationDate |
| `direction` | String | No | Dirección: asc, desc |

**Ejemplo:**

```bash
curl "http://localhost:8080/dvsmart_files_api/api/files/search?q=factura&tipoDocumento=FACTURA&anio=2025&page=0&size=20"
```

**Response (200 OK):**

```json
{
  "content": [
    {
      "idUnico": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
      "fileName": "factura_001.pdf",
      "fileSize": 1048576,
      "fileSizeFormatted": "1.00 MB",
      "lastModificationDate": "2025-12-15T10:30:00Z",
      "tipoDocumento": "FACTURA",
      "codigoCliente": "CLI001",
      "anio": 2025,
      "mes": 12
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1500,
  "totalPages": 75,
  "hasNext": true,
  "hasPrevious": false
}
```

### 🔵 Obtener Metadata

```http
GET /dvsmart_files_api/api/files/{idUnico}
```

**Ejemplo:**

```bash
curl "http://localhost:8080/dvsmart_files_api/api/files/a1b2c3d4e5f6..."
```

**Response (200 OK):**

```json
{
  "idUnico": "a1b2c3d4e5f6...",
  "fileName": "factura_001.pdf",
  "fileSize": 1048576,
  "fileSizeFormatted": "1.00 MB",
  "lastModificationDate": "2025-12-15T10:30:00Z",
  "reorgStatus": "COMPLETED",
  "reorgCompletedAt": "2025-12-20T14:30:00Z",
  "indexingStatus": "COMPLETED",
  "indexedAt": "2025-12-18T09:15:00Z",
  "business": {
    "tipoDocumento": "FACTURA",
    "codigoCliente": "CLI001",
    "anio": 2025,
    "mes": 12
  }
}
```

### 🔵 Descargar PDF

```http
GET /dvsmart_files_api/api/files/{idUnico}/download
```

**Response Headers:**
```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="factura_001.pdf"
Content-Length: 1048576
Accept-Ranges: bytes
```

**Ejemplo:**

```bash
curl -O -J "http://localhost:8080/dvsmart_files_api/api/files/a1b2c3d4.../download"
```

### 🔵 Ver PDF en Navegador

```http
GET /dvsmart_files_api/api/files/{idUnico}/view
```

**Response Headers:**
```http
Content-Type: application/pdf
Content-Disposition: inline; filename="factura_001.pdf"
```

> **Diferencia con `/download`:**
> - `/download` → `attachment` → Descarga el archivo
> - `/view` → `inline` → Abre en el visor del navegador

### 🔵 Preview de PDF

```http
GET /dvsmart_files_api/api/files/{idUnico}/preview
```

**Query Parameters:**

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `width` | Integer | 300 | Ancho en píxeles |
| `height` | Integer | 400 | Alto en píxeles |
| `page` | Integer | 1 | Página a renderizar |
| `format` | String | png | Formato: png, jpeg |

**Ejemplo:**

```bash
curl "http://localhost:8080/dvsmart_files_api/api/files/a1b2c3d4.../preview?width=400&height=500&page=1&format=png" -o preview.png
```

**Response:**
- `Content-Type: image/png` o `image/jpeg`
- Imagen binaria de la página renderizada

### 🔵 Estadísticas

```http
GET /dvsmart_files_api/api/files/stats
```

**Response:**

```json
{
  "totalFiles": 11000000,
  "totalSize": 5500000000000,
  "totalSizeFormatted": "5.00 TB",
  "byTipoDocumento": {
    "FACTURA": 5000000,
    "CONTRATO": 3000000,
    "RECIBO": 2000000,
    "OTROS": 1000000
  },
  "byStatus": {
    "COMPLETED": 10950000,
    "PENDING": 50000
  },
  "byYear": {
    "2025": 4000000,
    "2024": 3500000,
    "2023": 3500000
  }
}
```

### 🔵 Monitoreo Pool SFTP

```http
GET /dvsmart_files_api/api/monitoring/sftp-pool
```

**Response:**

```json
{
  "active": 2,
  "idle": 3,
  "maxTotal": 20,
  "totalCreated": 5,
  "totalDestroyed": 0,
  "totalBorrowed": 150,
  "totalReturned": 148,
  "utilizationPercent": 10.0,
  "availableSlots": 18
}
```

```http
GET /dvsmart_files_api/api/monitoring/sftp-pool/health
```

**Response:**

```json
{
  "status": "HEALTHY",
  "message": "Pool utilization is normal",
  "utilizationPercent": 10.0,
  "activeConnections": 2,
  "maxConnections": 20
}
```

**Estados posibles:**

| Status | Condición | Acción |
|--------|-----------|--------|
| `HEALTHY` | utilization < 80% | Normal |
| `WARNING` | utilization 80-95% | Aumentar pool |
| `CRITICAL` | utilization > 95% | Urgente |

---

## 📊 Monitorización y Observabilidad

### Health Checks

```bash
# Health general
curl http://localhost:8080/dvsmart_files_api/actuator/health | jq

# Debe mostrar:
# - MongoDB: UP
# - diskSpace: UP
```

### Métricas Disponibles

```bash
# Métricas JVM
curl http://localhost:8080/dvsmart_files_api/actuator/metrics/jvm.memory.used

# Métricas HTTP
curl http://localhost:8080/dvsmart_files_api/actuator/metrics/http.server.requests
```

### Swagger UI

```
http://localhost:8080/dvsmart_files_api/swagger-ui.html
```

### Logs Estructurados

**Ejemplo de log de descarga:**

```
2025-01-14 15:30:00 [http-nio-8080-exec-1] INFO  FileController - Download request: a1b2c3d4...
2025-01-14 15:30:00 [http-nio-8080-exec-1] DEBUG FileDownloadService - Starting download for file: a1b2c3d4...
2025-01-14 15:30:00 [http-nio-8080-exec-1] DEBUG FileMetadataMongoAdapter - Finding file by idUnico: a1b2c3d4...
2025-01-14 15:30:00 [http-nio-8080-exec-1] DEBUG SftpFileContentAdapter - Streaming file to output: /organized_data/a1/b2/c3/factura.pdf
2025-01-14 15:30:01 [http-nio-8080-exec-1] DEBUG SftpFileContentAdapter - Streamed 1048576 bytes for file: /organized_data/a1/b2/c3/factura.pdf
2025-01-14 15:30:01 [http-nio-8080-exec-1] INFO  FileController - Download completed: factura.pdf (1048576 bytes)
```

### Consultas Útiles en MongoDB

```javascript
// Conectar
mongo localhost:30000/dvsmart-ms -u dvsmart_user -p eoQQqfTyMd --authenticationDatabase dvsmart-ms

// Contar archivos disponibles
db.files_index.countDocuments({reorg_status: "COMPLETED"})

// Archivos por tipo de documento
db.files_index.aggregate([
  {$match: {reorg_status: "COMPLETED"}},
  {$group: {_id: "$business_tipoDocumento", count: {$sum: 1}}},
  {$sort: {count: -1}}
])

// Espacio total por año
db.files_index.aggregate([
  {$match: {reorg_status: "COMPLETED"}},
  {$group: {_id: "$business_anio", totalSize: {$sum: "$fileSize"}, count: {$sum: 1}}}
])
```

---

## 🔧 Troubleshooting

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `File not found` | idUnico inválido o no existe | Verificar con búsqueda primero |
| `File not available` | Reorganización no completada | Esperar a que `reorg_status=COMPLETED` |
| `SFTP connection failed` | Pool saturado o servidor caído | Revisar pool health, aumentar max-size |
| `Preview generation failed` | PDF corrupto o sin páginas | Verificar archivo original |
| `MongoDB connection refused` | BD no accesible | Verificar URI y credenciales |

### Comandos de Diagnóstico

```bash
# Verificar conectividad SFTP
telnet sftp-dest-host 30001

# Verificar MongoDB
mongo localhost:30000/dvsmart-ms --eval "db.files_index.countDocuments()"

# Verificar health de la aplicación
curl http://localhost:8080/dvsmart_files_api/actuator/health | jq

# Verificar pool SFTP
curl http://localhost:8080/dvsmart_files_api/api/monitoring/sftp-pool/health | jq

# Test de búsqueda
curl "http://localhost:8080/dvsmart_files_api/api/files/search?size=1" | jq
```

### Problemas de Rendimiento

```bash
# Si las descargas son lentas:
# 1. Verificar latencia de red al SFTP
ping sftp-dest-host

# 2. Verificar utilización del pool
curl http://localhost:8080/dvsmart_files_api/api/monitoring/sftp-pool | jq '.utilizationPercent'

# 3. Aumentar pool si utilización > 80%
# En application.properties:
# sftp.dest.pool.max-size=50
```

---

## 🧪 Mantenimiento y Testing

### Tests Unitarios

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=FileSearchServiceTest

# Tests con cobertura
mvn clean test jacoco:report
```

### Verificación de Endpoints

```bash
# Script de verificación rápida
echo "=== Health Check ==="
curl -s http://localhost:8080/dvsmart_files_api/actuator/health | jq '.status'

echo "=== Search Test ==="
curl -s "http://localhost:8080/dvsmart_files_api/api/files/search?size=1" | jq '.totalElements'

echo "=== Stats Test ==="
curl -s http://localhost:8080/dvsmart_files_api/api/files/stats | jq '.totalFiles'

echo "=== Pool Health ==="
curl -s http://localhost:8080/dvsmart_files_api/api/monitoring/sftp-pool/health | jq '.status'
```

---

## 📚 Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [Spring Integration SFTP](https://docs.spring.io/spring-integration/docs/current/reference/html/sftp.html)
- [Apache PDFBox](https://pdfbox.apache.org/docs/)
- [Apache Commons Pool2](https://commons.apache.org/proper/commons-pool/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

## 🤝 Soporte y Contacto

**Equipo de Mantenimiento**: DVSmart Team  
**Responsable Técnico**: hahuaranga@indracompany.com  
**Repositorio**: [Enlace interno al repositorio]  
**Documentación Técnica**: [Enlace a documentación detallada]